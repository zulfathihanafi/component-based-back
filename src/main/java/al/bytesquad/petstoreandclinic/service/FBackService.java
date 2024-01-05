package al.bytesquad.petstoreandclinic.service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import al.bytesquad.petstoreandclinic.entity.FBack;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.FBackDTO;
import al.bytesquad.petstoreandclinic.repository.FBackRepository;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

import java.security.Principal;
import java.util.List;

@Service
public class FBackService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final FBackRepository fbackRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;

    @Autowired
    public FBackService(ModelMapper modelMapper, UserRepository userRepository, FBackRepository fbackRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.fbackRepository = fbackRepository;
        this.objectMapper = objectMapper;
    }

    public List<FBack> getList() {
        return fbackRepository.findAll();
    }

    public FBack getFBackById(Long id) {
        return fbackRepository.findById(id).orElse(null);
    }

    public List<FBack> getByMonth(String month) {
        return fbackRepository.findByMonth(month);
    }

    public FBack create(String fbackString) throws JsonProcessingException {
        FBackDTO fbackDTO = objectMapper.readValue(fbackString, FBackDTO.class);
        FBack fback = modelMapper.map(fbackDTO, FBack.class);
        fback.setUser(userRepository.findUserById(fbackDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(fbackString, fbackString, 0)));
        fback.setMonth(fbackDTO.getMonth());

        // Perform sentiment analysis
        SentimentAnalysisResult titleSentiment = analyzeSentiment(fback.getTitle());
        SentimentAnalysisResult messageSentiment = analyzeSentiment(fback.getMessage());

        // Set sentiment fields
        fback.setTitleSentiment(titleSentiment.getSentiment());
        fback.setMessageSentiment(messageSentiment.getSentiment());

        return fbackRepository.save(fback);
    }

    public FBack update(String fbackString, long id) throws JsonProcessingException {
        FBackDTO fbackDTO = objectMapper.readValue(fbackString, FBackDTO.class);
        FBack fback = fbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(fbackString, fbackString, 0));

        fback.setTitle(fbackDTO.getTitle());
        fback.setMessage(fbackDTO.getMessage());
        fback.setMonth(fbackDTO.getMonth());

        return fbackRepository.save(fback);
    }

    public void delete(long id) {
        fbackRepository.deleteById(id);
    }

    private SentimentAnalysisResult analyzeSentiment(String text) {
        Annotation annotation = new Annotation(text);
        stanfordCoreNLP.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null && !sentences.isEmpty()) {
            CoreMap sentence = sentences.get(0);
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            return new SentimentAnalysisResult(sentiment);
        }

        return new SentimentAnalysisResult("Unknown");
    }

    // Overall sentiment by month
    public String getOverallSentimentByMonth(String month) {
        List<FBack> fbacks = fbackRepository.findByMonth(month);

        int totalFeedbacks = fbacks.size();
        int positiveFeedbacks = 0;
        int neutralFeedbacks = 0;
        int negativeFeedbacks = 0;

        for (FBack fback : fbacks) {
            SentimentAnalysisResult titleSentiment = analyzeSentiment(fback.getTitle());
            SentimentAnalysisResult messageSentiment = analyzeSentiment(fback.getMessage());

            // Determine the overall sentiment of the feedback
            String overallSentiment = determineOverallSentiment(titleSentiment.getSentiment(), messageSentiment.getSentiment());

            // Update counts based on overall sentiment
            switch (overallSentiment) {
                case "Positive":
                    positiveFeedbacks++;
                    break;
                case "Neutral":
                    neutralFeedbacks++;
                    break;
                case "Negative":
                    negativeFeedbacks++;
                    break;
            }
        }

        // Determine the overall sentiment of the month based on counts
        return determineOverallSentiment(positiveFeedbacks, neutralFeedbacks, negativeFeedbacks, totalFeedbacks);
    }

    private String determineOverallSentiment(String titleSentiment, String messageSentiment) {
        // Implement logic to determine overall sentiment based on title and message sentiments
        // For simplicity, let's say if either title or message is negative, consider overall sentiment as negative
        if ("Negative".equals(titleSentiment) || "Negative".equals(messageSentiment)) {
            return "Negative";
        } else if ("Positive".equals(titleSentiment) || "Positive".equals(messageSentiment)) {
            return "Positive";
        } else {
            return "Neutral";
        }
    }

    private String determineOverallSentiment(int positiveFeedbacks, int neutralFeedbacks, int negativeFeedbacks, int totalFeedbacks) {
        // Implement logic to determine overall sentiment based on counts
        // For simplicity, let's say if more than half of the feedbacks are negative, consider overall sentiment as negative
        if (negativeFeedbacks > totalFeedbacks / 2) {
            return "Negative";
        } else if (positiveFeedbacks > totalFeedbacks / 2) {
            return "Positive";
        } else {
            return "Neutral";
        }
    }

    class SentimentAnalysisResult {
        private String sentiment;

        public SentimentAnalysisResult(String sentiment) {
            this.sentiment = sentiment;
        }

        public String getSentiment() {
            return sentiment;
        }
    }
}
