package com.drcnet.highway.service;

import com.drcnet.highway.config.LocalVariableConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @Author jack
 * @Date: 2019/8/16 16:19
 * @Desc: 脚本执行util
 **/
@Component
@Slf4j
public class ScriptExecuteService {
    @Resource
    private LocalVariableConfig localVariableConfig;

    public void executeArithmeticScript(Integer taskId) {
        try {
            log.info("start execute ArithmeticScript...............，scriptPath: {},  taskId:{}", localVariableConfig.getScriptPath(), taskId);

            StringBuilder path = new StringBuilder(localVariableConfig.getScriptPath());
            path.append(" ").append(taskId);
            Process ps = Runtime.getRuntime().exec(path.toString());
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println(result);
            log.info("数据刷新成功");

            /*String[] args1 = new String[]{"/usr/local/python3/bin/python3.6", localVariableConfig.getScriptPath(), String.valueOf(taskId)};
            Process pr = Runtime.getRuntime().exec(args1);

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            pr.waitFor();*/
            log.info("end execute ArithmeticScript...............");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void testExecuteImportSqlShell() {
        try {
            log.info("start execute ImportSqlScript...............");
            String scriptPath = "/project/sql/";
            String sqlPath = "/project/sql/";
            StringBuilder path = new StringBuilder(scriptPath);
            path.append(localVariableConfig.getRoadName()).append(".sh").append(" ").append(sqlPath)
                    .append(localVariableConfig.getRoadName()).append(".sql");
            log.info("path: {}", path.toString());
            Process ps = Runtime.getRuntime().exec(path.toString());
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println(result);
            log.info("数据执行成功");

            log.info("end execute ImportSqlScript...............");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
