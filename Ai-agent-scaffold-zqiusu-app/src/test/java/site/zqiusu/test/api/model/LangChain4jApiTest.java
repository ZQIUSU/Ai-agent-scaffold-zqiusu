package site.zqiusu.test.api.model;


import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LangChain4jApiTest {
    public static void main(String[] args) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://apis.itedus.cn/v1")
                .apiKey("sk-zNFxIUzKyiZeX5jl053bEdEcDb1b47E0921c7dC145Ee9f96")
                .modelName("gpt-4o")
                .build();

        String chat = chatModel.chat("你使用的是什么模型？");

        log.info("测试结果:{}",chat);

    }
}
