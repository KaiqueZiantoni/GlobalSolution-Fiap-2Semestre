package br.com.fiap.tomeidoutor.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ModelMapper usado pelos controllers para converter DTO em model e model em DTO.
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                // STRICT: so copia campos de mesmo nome. Evita que "medicamentoId" do DTO
                // seja confundido com o "id" do model.
                .setMatchingStrategy(MatchingStrategies.STRICT)
                // Campo opcional que veio vazio nao apaga o valor padrao do model
                // (posologia ativa, dose SEM_REGISTRO).
                .setSkipNullEnabled(true);
        return mapper;
    }
}
