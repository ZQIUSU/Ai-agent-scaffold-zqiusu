package site.zqiusu.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.zqiusu.domain.agent.model.entity.ArmoryCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.service.armory.AbstractArmorySupport;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;

@Slf4j
@Service
public class RunnerNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent装配 - ChatModelNode");
        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        String appName = aiAgentConfigTableVO.getAppName();
        String agentId = aiAgentConfigTableVO.getAgent().getAgentId();
        String agentName = aiAgentConfigTableVO.getAgent().getAgentName();
        String agentDesc = aiAgentConfigTableVO.getAgent().getAgentDesc();

        SequentialAgent sequentialAgent = dynamicContext.getSequentialAgent();

        InMemoryRunner runner = new InMemoryRunner(sequentialAgent, appName);

        AiAgentRegisterVO aiAgentRegisterVO = AiAgentRegisterVO.builder()
                .appName(appName)
                .agentId(agentId)
                .agentName(agentName)
                .agentDesc(agentDesc)
                .runner(runner)
                .build();

        registerBean(agentId,AiAgentRegisterVO.class,aiAgentRegisterVO);

        return aiAgentRegisterVO;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
