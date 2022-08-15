package com.lxh.springbatchtransaction;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Date;

@SpringBootApplication
public class SpringBatchTransactionApplication {

    public static void main(String[] args) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        ConfigurableApplicationContext run = SpringApplication.run(SpringBatchTransactionApplication.class, args);
        JobLauncher jobLauncher = (JobLauncher)run.getBean("jobLauncher");
        Job myJob = run.getBean("myJob",Job.class);
        //    后置参数：使用JobParameters中绑定参数 addLong  addString 等方法
        JobParameters jobParameters = new JobParametersBuilder().addDate("date", new Date()).toJobParameters();
        jobLauncher.run(myJob, jobParameters);
    }

}
