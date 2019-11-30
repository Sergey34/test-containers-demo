package seko.es.join.service.configuration

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import seko.es.join.service.services.ElasticsearchItemReader
import seko.es.join.service.services.EsItemWriter


@Configuration
@EnableBatchProcessing
class BatchConfiguration {

    @Bean
    fun reader(client: RestHighLevelClient): ElasticsearchItemReader {
        val searchRequest = SearchRequest("my_index")
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(QueryBuilders.matchAllQuery())
        searchRequest.source(searchSourceBuilder)
        val elasticsearchItemReader = ElasticsearchItemReader(client, searchRequest)
        elasticsearchItemReader.setName("test")
        return elasticsearchItemReader
    }

    @Bean
    fun writer(client: RestHighLevelClient): EsItemWriter {
        return EsItemWriter(client)
    }


    @Bean
    fun importUserJob(jobs: JobBuilderFactory, s1: Step): Job {
        return jobs["importUserJob"]
            .incrementer(RunIdIncrementer())
            .flow(s1)
            .end()
            .build()
    }

    @Bean
    fun step1(
        stepBuilderFactory: StepBuilderFactory,
        reader: ElasticsearchItemReader,
        writer: EsItemWriter
    ): Step {
        return stepBuilderFactory["step1"]
            .chunk<Map<*, *>, Map<*, *>>(10)
            .reader(reader)
//            .processor(processor)
            .writer(writer)
            .build()
    }


}