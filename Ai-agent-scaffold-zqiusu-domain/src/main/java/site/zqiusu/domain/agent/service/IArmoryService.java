package site.zqiusu.domain.agent.service;

import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;

import java.util.List;

//提供接口
public interface IArmoryService {

    //装配Agent
    void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception;
}
