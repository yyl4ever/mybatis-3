package com.company.bbkb.mybatis;

import com.company.bbkb.entity.TtlProductInfoPo;
import com.company.bbkb.mapper.TtlProductInfoMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.resolution.cachereffromxml.UserMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Author: yangyl
 * @Date: 2020-05-03 10:13
 * @Description:
 */
public class MybatisTest {
  private static final String resource = "mybatis/mybatis-config.xml";

  public static void main(String[] args) throws IOException {
    InputStream is = Resources.getResourceAsStream(resource);
    SqlSessionFactory ssf = new SqlSessionFactoryBuilder().build(is);

//    SqlSession sqlSession = ssf.openSession();
//    try {
//      List<TtlProductInfoPo> products = sqlSession.selectList("com.company.bbkb.mapper.TtlProductInfoMapper.listProduct");
//      System.out.println(products);
//    } finally {
//      sqlSession.close();
//    }

    try (SqlSession sqlSession = ssf.openSession()) {
      // 包名.类名.方法名，与 xml 中配置的 namespace 一致
      // 如何打印 sql?
      List<TtlProductInfoPo> products = sqlSession.selectList("com.company.bbkb.mapper.TtlProductInfoMapper.listProduct");
      System.out.println(products);
    }
  }

  @Test
  public void testAdd() throws IOException {
    //创建sessionFactory对象
    SqlSessionFactory sf = new SqlSessionFactoryBuilder()
      .build(Resources.getResourceAsStream(resource));
    //获取session对象
    SqlSession session = sf.openSession();
    //创建实体对象
    TtlProductInfoPo po = new TtlProductInfoPo();
    po.setProductName("toby2");
    po.setCategoryName("123");
    po.setCategoryId(23L);
    //保存数据到数据库中
    session.insert("com.company.bbkb.mapper.TtlProductInfoMapper.add", po);
    //提交事务,这个是必须要的,否则即使sql发了也保存不到数据库中
    session.commit();
    //关闭资源
    session.close();
  }

  @Test
  public void testGetObject() throws Exception {
    SqlSessionFactory sf = new SqlSessionFactoryBuilder()
      .build(Resources.getResourceAsStream(resource));
    //获取session对象
    SqlSession session = sf.openSession();
    TtlProductInfoMapper mapper = session.getMapper(TtlProductInfoMapper.class);
    TtlProductInfoPo ttlProductInfoPo = mapper.get(1L);
    System.out.println(ttlProductInfoPo);
    session.close();
  }
}
