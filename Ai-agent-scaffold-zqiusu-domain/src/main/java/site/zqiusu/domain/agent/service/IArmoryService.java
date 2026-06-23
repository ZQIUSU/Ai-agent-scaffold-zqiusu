package site.zqiusu.domain.agent.service;

import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;

import java.util.List;

public interface IArmoryService {


    void acceptArmoryAgents(List<AiAgentConfigTableVO> tables) throws Exception;
}
