package com.qlh.base;

/**
 * @description: 工厂状态
 */
public enum LaborProcessTypeEnum implements QlhEnum<LaborProcessTypeEnum, String> {
    Type1("01", "正常"),
    Type2("02", "量试导入"),
    Type3("03", "新产线"),
    Type4("04", "首厂中试"),
    Type5("05", "新品新厂"),
    ;

    LaborProcessTypeEnum(String code, String desc) {
        init(code, desc);
    }
}
