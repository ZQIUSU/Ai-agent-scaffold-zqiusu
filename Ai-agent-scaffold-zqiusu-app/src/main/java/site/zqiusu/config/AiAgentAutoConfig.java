package site.zqiusu.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import site.zqiusu.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import site.zqiusu.domain.agent.service.IArmoryService;

import javax.annotation.Resource;
import java.util.ArrayList;

@Slf4j
@Configuration
@EnableConfigurationProperties(AiAgentAutoConfigProperties.class)
public class AiAgentAutoConfig {

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    @Resource
    private IArmoryService armoryService;

    //监听服务是否启动，启动后开始装配
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent(ApplicationReadyEvent event) {

        //自动装配
        if (!aiAgentAutoConfigProperties.isEnabled()) {
            log.info("Ai Agent 自动装配未启用");
            return;
        }

        try {
            log.info("Ai Agent 智能体装配 {}", JSON.toJSONString(aiAgentAutoConfigProperties.getTables().values()));

            armoryService.acceptArmoryAgents(new ArrayList<>(aiAgentAutoConfigProperties.getTables().values()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

