package com.viking.mapper;


import com.viking.mymapper.MyMapper;
import com.viking.pojo.Fans;
import com.viking.vo.FansVO;
import com.viking.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FansMapperCustom extends MyMapper<Fans> {

    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}