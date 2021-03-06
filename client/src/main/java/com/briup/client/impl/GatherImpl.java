package com.briup.client.impl;

import com.briup.aware.ConfigurationAware;
import com.briup.bean.Environment;
import com.briup.bean.EnvironmentFactory;
import com.briup.client.Gather;
import com.briup.client.Logger;
import com.briup.conf.Configuration;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;


public class GatherImpl implements Gather, ConfigurationAware {
    private String target;
    private String record;
    private Logger logger;
    @Override
    public Collection<Environment> gather(){
        Collection<Environment> coll = new ArrayList<>();
        long flag = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(target));
            flag = new DataInputStream(new FileInputStream(record)).readLong();
            String st = null;
            br.skip(flag);
            while ((st = br.readLine()) != null) {
                try {
//                    if ((st = br.readLine()) == null){
//                        Thread.sleep(36000);
//                        continue;
//                    }
                    String[] sp = st.split("[|]");
                    switch (Integer.parseInt(sp[3])) {
                        case 16:
                            Environment humiEn = EnvironmentFactory.getHumiEn(sp[0], sp[1], sp[2], sp[3], Integer.parseInt(sp[4]), sp[5], Integer.parseInt(sp[7]), sp[6], new Timestamp(Long.parseLong(sp[8])));
                            Environment tempEn = EnvironmentFactory.getTempEn(sp[0], sp[1], sp[2], sp[3], Integer.parseInt(sp[4]), sp[5], Integer.parseInt(sp[7]), sp[6], new Timestamp(Long.parseLong(sp[8])));
                            coll.add(humiEn);
                            coll.add(tempEn);
                            break;
                        case 256:
                            Environment sunEn = EnvironmentFactory.getSunEn(sp[0], sp[1], sp[2], sp[3], Integer.parseInt(sp[4]), sp[5], Integer.parseInt(sp[7]), sp[6], new Timestamp(Long.parseLong(sp[8])));
                            coll.add(sunEn);
                            break;
                        case 1280:
                            Environment co2En = EnvironmentFactory.getCO2En(sp[0], sp[1], sp[2], sp[3], Integer.parseInt(sp[4]), sp[5], Integer.parseInt(sp[7]), sp[6], new Timestamp(Long.parseLong(sp[8])));
                            coll.add(co2En);
                            break;
                        default:
                            throw new Exception("参数错误，请检查文件");
                    }
                    flag+=st.length()+1;
                    //累加本次读取字符长度
                } catch (Exception e) {
                    flag+=st.length()+1;
                    continue;
                }
            }
            logger.info("文件读取完成，共读取"+coll.size()+"条数据");
        } catch (FileNotFoundException e) {
            logger.error("文件未找到："+e.getMessage());
        } catch (IOException e) {
            logger.error("Gather传输流错误");
        } catch (Exception e){
            logger.error(e.getMessage());
        }
        //本次读取文件结束，flag需要持久化保存
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(record));
            dos.writeLong(flag);
            dos.flush();
            dos.close();
            logger.info("Gather流关闭");
        } catch (FileNotFoundException e) {
            logger.error("文件异常"+e.getMessage());
        } catch (IOException e) {
            logger.error("流关闭异常"+e.getMessage());
        }
        return coll;
    }

    @Override
    public void init(Properties properties) {
        target = properties.getProperty("target");
        record = properties.getProperty("record");
    }

    @Override
    public void setConfiguration(Configuration paramConfiguration) {
        try {
            logger=paramConfiguration.getLogger();
        } catch (Exception e) {
            System.out.println("获取logger实例失败");
        }
    }
}
