package site.zqiusu.test.domain.agent;


import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import site.zqiusu.domain.agent.model.entity.ChatCommandEntity;
import site.zqiusu.domain.agent.service.IChatService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatServiceTest {

    @Resource
    private IChatService chatService;

    @Value("classpath:dog.png")
    private org.springframework.core.io.Resource imageResource;

    @Test
    public void test_handleMessage01(){
        List<String> message = chatService.handleMessage("100003", "zzp", "你好，你有什么功能？");
        log.info("测试结果1:{}", JSON.toJSONString(message));
    }

    @Test
    public void test_handleMessage02() throws IOException {
        String agentId = "100003";
        String userId = "zzp";
        String sessionId = chatService.createSession(agentId, userId);

        ChatCommandEntity chatCommandEntity = ChatCommandEntity.builder()
                .agentId(agentId)
                .userId(userId)
                .sessionId(sessionId)
                .texts(List.of(new ChatCommandEntity.Content.Text("请告诉我这个是什么动物？")))
                .files(List.of())
                .inlineDatas(List.of(new ChatCommandEntity.Content.InlineData(imageResource.getContentAsByteArray(), MimeTypeUtils.IMAGE_PNG_VALUE)))
                .build();

        List<String> strings = chatService.handleMessage(chatCommandEntity);
        log.info("测试结果2:{}", JSON.toJSONString(strings));
    }
}
