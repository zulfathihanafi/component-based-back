package al.bytesquad.petstoreandclinic.service;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import al.bytesquad.petstoreandclinic.entity.User;
import al.bytesquad.petstoreandclinic.payload.entityDTO.UserDTO;
import al.bytesquad.petstoreandclinic.repository.UserRepository;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OnnxTensor;
@Service
public class NLPService {
    
    private static final String ONNX_FILENAME = "decision_tree_model_with_cv.onnx";
    
    @Autowired
    public NLPService() {
    }

    public String analyse(String text){
        OrtEnvironment env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        OrtSession onnxSession = null;
        
        try {
            onnxSession = env.createSession(ONNX_FILENAME, opts);
        } catch (OrtException e) {
            e.printStackTrace();
        }
        // Input text to transform
        String[] inputText = {text};

        try {
            // Prepare input tensor
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputText);
            
            // Run the ONNX model for the entire pipeline
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("text_input", inputTensor);
            Result result = onnxSession.run(inputs);

            // Getting the result
            OnnxTensor tensor = (OnnxTensor) result.get(0);
            String[] stringData = (String[]) tensor.getValue();
            System.out.println(stringData[0]);
            result.close();

            return stringData[0];
        } catch (OrtException e) {
            e.printStackTrace();
        }

        return "Some error occured";
    }
}
