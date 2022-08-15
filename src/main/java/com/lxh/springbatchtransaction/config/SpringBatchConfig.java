package com.lxh.springbatchtransaction.config;

import com.lxh.springbatchtransaction.listener.MyJobListener;
import com.lxh.springbatchtransaction.listener.MyReadListener;
import com.lxh.springbatchtransaction.listener.MyWriteListener;
import com.lxh.springbatchtransaction.model.Transaction;
import com.lxh.springbatchtransaction.processor.MyItemProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {


    /**
     * JobRepository定义：Job的注册容器以及和数据库打交道（事务管理等）
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */

    @Bean
    public JobRepository myJobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDatabaseType("mysql");
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setDataSource(dataSource);
        return jobRepositoryFactoryBean.getObject();
    }

    /**
     * jobLauncher定义： job的启动器,绑定相关的jobRepository
     * @param dataSource
     * @param transactionManager
     * @return
     * @throws Exception
     */
    @Bean
    public SimpleJobLauncher myJobLauncher(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        // 设置jobRepository
        jobLauncher.setJobRepository(myJobRepository(dataSource, transactionManager));
        return jobLauncher;
    }


    /**
     * 定义job
     * @param jobs
     * @param myStep
     * @return
     */
    @Bean
    public Job myJob(JobBuilderFactory jobs, Step myStep) {
        return jobs.get("myJob")
                .incrementer(new RunIdIncrementer())
                .flow(myStep)
                .end()
                .listener(myJobListener())
                .build();
    }


    /**
     * 注册job监听器
     * @return
     */
    @Bean
    public MyJobListener myJobListener() {
        return new MyJobListener();
    }

    /**
     * ItemReader定义：读取文件数据+entirty实体类映射
     * @return
     */
    @Bean
    public ItemReader<Transaction> reader() {
        // 使用FlatFileItemReader去读cvs文件，一行即一条数据
        FlatFileItemReader<Transaction> reader = new FlatFileItemReader<>();
//        // 由于csv文件第一行是标题，因此通过setLinesToSkip方法设置跳过一行
//        reader.setLinesToSkip(1);
        // setResource方法配置csv文件的位置
        reader.setResource(new ClassPathResource("static/交易流水.csv"));
        // entity与csv数据做映射
        reader.setLineMapper(new DefaultLineMapper<Transaction>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        // setNames方法配置了csv文件一共有4列
                        setNames(new String[]{"cardId", "transactionDate", "transactionAmount", "tradingTime"});
//                        // 配置列与列之间的间隔符
//                        setDelimiter(",");
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<Transaction>() {
                    {
                        setTargetType(Transaction.class);//设置实体，实体的field名称必须和tokenizer.names一致
                    }
                });
            }
        });
        return reader;
    }

    /**
     * 注册ItemProcessor: 处理数据+校验数据
     *
     * @return
     */
    @Bean
    public ItemProcessor<Transaction, Transaction> processor() {
        MyItemProcessor myItemProcessor = new MyItemProcessor();
        // 设置校验器
//        myItemProcessor.setValidator(myBeanValidator());
        return myItemProcessor;
    }

//    /**
//     * 注册校验器
//     *
//     * @return
//     */
//    @Bean
//    public MyBeanValidator myBeanValidator() {
//        return new MyBeanValidator<Transaction>();
//    }

    /**
     * ItemWriter定义：指定datasource，设置批量插入sql语句，写入数据库
     *
     * @param dataSource
     * @return
     */
    @Bean
    public ItemWriter<Transaction> writer(DataSource dataSource) {
        // 使用的JdbcBatchltemWriter则是通过JDBC将数据写出到一个关系型数据库中。
        JdbcBatchItemWriter<Transaction> writer = new JdbcBatchItemWriter<>();
        // 设置有参数的sql语句
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        String sql = "insert into transaction(card_id,transaction_date,transaction_amount,trading_time) " +
                "values(:cardId, :transactionDate, :transactionAmount, :tradingTime)";
        writer.setSql(sql);
        // 配置使用的数据源
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Step myStep(StepBuilderFactory stepBuilderFactory, ItemReader<Transaction> reader,
                       ItemWriter<Transaction> writer, ItemProcessor<Transaction, Transaction> processor) {
        return stepBuilderFactory
                .get("myStep")
                .<Transaction, Transaction>chunk(2) // Chunk的机制(即每次读取一条数据，再处理一条数据，累积到一定数量后再一次性交给writer进行写入操作)
                .reader(reader).faultTolerant().retryLimit(3).retry(Exception.class).skip(Exception.class).skipLimit(2)
                .listener(new MyReadListener())
                .processor(processor)
                .writer(writer).faultTolerant().skip(Exception.class).skipLimit(2)
                .listener(new MyWriteListener())
                .build();
    }


}
