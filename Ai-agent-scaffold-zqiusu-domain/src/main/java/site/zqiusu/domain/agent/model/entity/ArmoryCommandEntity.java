package site.zqiusu.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmoryCommandEntity {

    private AiAgentConfigTableVO aiAgentConfigTableVO;
}
