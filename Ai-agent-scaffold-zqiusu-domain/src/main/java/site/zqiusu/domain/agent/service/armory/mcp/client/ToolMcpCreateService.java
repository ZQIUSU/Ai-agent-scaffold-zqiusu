package site.zqiusu.domain.agent.service.armory.mcp.client;

import org.springframework.ai.tool.ToolCallback;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;

public interface ToolMcpCreateService {

    ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception;

}
