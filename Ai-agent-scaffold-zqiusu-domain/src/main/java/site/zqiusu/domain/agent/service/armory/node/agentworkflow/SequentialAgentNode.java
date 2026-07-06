package site.zqiusu.domain.agent.service.armory.node.agentworkflow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import site.zqiusu.domain.agent.model.entity.ArmoryCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.service.armory.AbstractArmorySupport;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;

public class SequentialAgentNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
