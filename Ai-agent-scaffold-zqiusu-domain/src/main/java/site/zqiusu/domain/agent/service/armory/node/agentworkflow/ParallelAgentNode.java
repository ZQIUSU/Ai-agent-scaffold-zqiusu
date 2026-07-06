package site.zqiusu.domain.agent.service.armory.node.agentworkflow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import site.zqiusu.domain.agent.model.entity.ArmoryCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.model.valobj.enums.AgentTypeEnum;
import site.zqiusu.domain.agent.service.armory.AbstractArmorySupport;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;

import java.util.List;

public class ParallelAgentNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
        if (agentWorkflows == null || agentWorkflows.isEmpty()){
            return defaultStrategyHandler;
        }

        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.get(0);
        String type = agentWorkflow.getType();
        AgentTypeEnum agentTypeEnum = AgentTypeEnum.formType(type);

        if( agentTypeEnum == null ){
            throw new IllegalArgumentException("AgentWorkFlow type is Error");
        }

        String node = agentTypeEnum.getNode();
        return switch(node){
            case "loopAgentNode" -> getBean("loopAgentNode");
            case "sequentialAgentNode" -> getBean("sequentialAgentNode");
            default -> defaultStrategyHandler;
        };
    }
}
