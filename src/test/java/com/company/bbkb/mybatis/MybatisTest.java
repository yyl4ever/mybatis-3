package com.company.bbkb.mybatis;

import com.company.bbkb.entity.TtlProductInfoPo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Author: yangyl
 * @Date: 2020-05-03 10:13
 * @Description:
 */
public class MybatisTest {
  public static void main(String[] args) throws IOException {
    String resource = "mybatis/mybatis-config.xml";
    InputStream is = Resources.getResourceAsStream(resource);

    SqlSessionFactory ssf = new SqlSessionFactoryBuilder().build(is);
    SqlSession sqlSession = ssf.openSession();

    try {
      List<TtlProductInfoPo> products = sqlSession.selectList("com.company.bbkb.mapper.TtlProductInfoMapper.listProduct");
      System.out.println(products);
    } finally {
      sqlSession.close();
    }
  }

}
