package site.zqiusu.test.api.model;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

@Slf4j
public class SpringAiApiTest {
    public static void main(String[] args) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://apis.itedus.cn")
                .apiKey("sk-zNFxIUzKyiZeX5jl053bEdEcDb1b47E0921c7dC145Ee9f96")
                .completionsPath("v1/chat/completions")
                .embeddingsPath("v1/embeddings")
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4.1")
                        .build())
                .build();

        String call = chatModel.call("你好！你是谁？");

        log.info("测试结果,{}",call);

    }
}
