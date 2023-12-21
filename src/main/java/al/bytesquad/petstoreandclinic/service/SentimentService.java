//Sentiment Service - create class to initialize Stanford CoreNLP

package al.bytesquad.petstoreandclinic.service;

import org.modelmapper.ModelMapper;
import org.modelmapper.ModelMapper; 
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//entity
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.UserDTO;

//repo
import al.bytesquad.petstoreandclinic.repository.UserRepository;


//sus
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


//import repository and entity
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import al.bytesquad.petstoreandclinic.service.exception.ResourceNotFoundException;

//stanford
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;


@Service
public class SentimentService {

    private final StanfordCoreNLP pipeline;

    public SentimentService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String text) {
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            int sentiment = RNNCoreAnnotations.getPredictedClass(sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class));
            switch (sentiment) {
                case 0:
                    return "Very Negative";
                case 1:
                    return "Negative";
                case 2:
                    return "Neutral";
                case 3:
                    return "Positive";
                case 4:
                    return "Very Positive";
                default:
                    return "Unknown";
            }
        }
        return "Unknown";
    }
}


