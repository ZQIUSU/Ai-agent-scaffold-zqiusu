package site.zqiusu.domain.agent.model.valobj.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AgentTypeEnum {

    Loop("循环执行","Loop","loopAgentNode"),
    Parallel("并行执行","Parallel","parallelAgentNode"),
    Sequential("串行执行","Sequential","sequentialAgentNode")
    ;
    private String name;
    private String type;
    private String node;

    public static AgentTypeEnum formType(String type){
        if(type == null){
            return null;
        }

        for(AgentTypeEnum value: values()){
            if(value.getType().equalsIgnoreCase(type)){
                return value;
            }

        }

        return null;
    }
}
