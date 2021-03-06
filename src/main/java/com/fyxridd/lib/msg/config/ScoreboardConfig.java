package com.fyxridd.lib.msg.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.core.api.config.basic.ListType;
import com.fyxridd.lib.core.api.config.basic.Path;
import com.fyxridd.lib.core.api.config.convert.ListConvert;
import com.fyxridd.lib.core.api.config.convert.ListConvert.ListConverter;
import com.fyxridd.lib.core.api.config.limit.Max;
import com.fyxridd.lib.core.api.config.limit.Min;
import com.fyxridd.lib.msg.model.SideConfig;

@Path("scoreboard")
public class ScoreboardConfig {
    private static class SidesConverter implements ListConverter<Map<Integer, SideConfig>> {
        @Override
        public Map<Integer, SideConfig> convert(String plugin, List list) {
            Map<Integer, SideConfig> sides = new HashMap<>();
            for (Object o:list) {
                String[] args = ((String)o).split(" ");
                int line = Integer.parseInt(args[0]);
                String name = args[1];
                String data;
                if (args.length <= 2)  data = "";
                else data = UtilApi.combine(args, " ", 2, args.length);

                sides.put(line, new SideConfig(name, data));
            }
            return sides;
        }
        
    }
    
    @Path("side.size")
    @Min(0)
    @Max(16)
    private int sideSize;
    //行号(从0开始) 侧边栏获取器配置
    @Path("side.sides")
    @ListConvert(listType = ListType.String, value = SidesConverter.class)
    private Map<Integer, SideConfig> sides;
    
    public int getSideSize() {
        return sideSize;
    }

    public Map<Integer, SideConfig> getSides() {
        return sides;
    }
}
