package br.com.fiap.tomeidoutor.dao;

import br.com.fiap.tomeidoutor.exception.AcessoDadosException;
import br.com.fiap.tomeidoutor.model.OrigemMedicamento;
import br.com.fiap.tomeidoutor.model.PeriodoDia;
import br.com.fiap.tomeidoutor.model.Posologia;
import br.com.fiap.tomeidoutor.model.TipoUso;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tb_posologia com JDBC puro.
 * A coluna cd_paciente e a chave estrangeira do 1:N paciente -> posologia.
 */
@Repository
public class PosologiaDao {

    private static final String SEQUENCE = "sq_tb_posologia";
    private static final String COLUNAS = "cd_posologia, cd_paciente, cd_medicamento, ds_periodo, qt_dose, "
            + "tp_uso, tp_origem, dt_inicio, dt_fim, st_ativo";

    private final DataSource dataSource;

    public PosologiaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava a posologia com o proximo id da sequence e devolve o mesmo objeto, ja com o id. */
    public Posologia inserir(Posologia posologia) {
        String sql = "INSERT INTO tb_posologia (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conexao, SEQUENCE);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, posologia.getPacienteId());
                ps.setLong(3, posologia.getMedicamentoId());
                ps.setString(4, posologia.getPeriodo().name());
                ps.setBigDecimal(5, posologia.getQuantidadeDose());
                ps.setString(6, posologia.getTipoUso().name());
                ps.setString(7, posologia.getOrigem().name());
                ps.setDate(8, JdbcUtil.paraDate(posologia.getDataInicio()));
                ps.setDate(9, JdbcUtil.paraDate(posologia.getDataFim()));
                ps.setString(10, JdbcUtil.paraSimNao(posologia.isAtivo()));
                ps.executeUpdate();
            }
            posologia.setId(id);
            return posologia;
        } catch (SQLException e) {
            throw new AcessoDadosException("cadastrar posologia", e);
        }
    }

    /** Lado N do 1:N: todas as posologias de um paciente. */
    public List<Posologia> listarPorPaciente(Long pacienteId) {
        String sql = "SELECT " + COLUNAS + " FROM tb_posologia WHERE cd_paciente = ? ORDER BY cd_posologia";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Posologia> posologias = new ArrayList<>();
                while (rs.next()) {
                    posologias.add(montar(rs));
                }
                return posologias;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("listar posologias do paciente", e);
        }
    }

    /** Devolve null quando o id nao existe. */
    public Posologia buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_posologia WHERE cd_posologia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montar(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("buscar posologia", e);
        }
    }

    public boolean existe(Long id) {
        String sql = "SELECT 1 FROM tb_posologia WHERE cd_posologia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar posologia", e);
        }
    }

    /**
     * Regra de negocio: diz se o paciente ja tem posologia ATIVA do mesmo medicamento no mesmo periodo.
     *
     * @param ignorarId id da propria posologia na atualizacao; nulo no cadastro
     */
    public boolean existeAtiva(Long pacienteId, Long medicamentoId, PeriodoDia periodo, Long ignorarId) {
        String sql = "SELECT 1 FROM tb_posologia WHERE cd_paciente = ? AND cd_medicamento = ? "
                + "AND ds_periodo = ? AND st_ativo = 'S' AND cd_posologia <> ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, pacienteId);
            ps.setLong(2, medicamentoId);
            ps.setString(3, periodo.name());
            ps.setLong(4, ignorarId == null ? -1L : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar posologia duplicada", e);
        }
    }

    public int contarPorPaciente(Long pacienteId) {
        return contar("SELECT COUNT(*) FROM tb_posologia WHERE cd_paciente = ?", pacienteId);
    }

    public int contarPorMedicamento(Long medicamentoId) {
        return contar("SELECT COUNT(*) FROM tb_posologia WHERE cd_medicamento = ?", medicamentoId);
    }

    /** Devolve false quando o id nao existe. O paciente da posologia nao muda. */
    public boolean atualizar(Posologia posologia) {
        String sql = "UPDATE tb_posologia SET cd_medicamento = ?, ds_periodo = ?, qt_dose = ?, tp_uso = ?, "
                + "tp_origem = ?, dt_inicio = ?, dt_fim = ?, st_ativo = ? WHERE cd_posologia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, posologia.getMedicamentoId());
            ps.setString(2, posologia.getPeriodo().name());
            ps.setBigDecimal(3, posologia.getQuantidadeDose());
            ps.setString(4, posologia.getTipoUso().name());
            ps.setString(5, posologia.getOrigem().name());
            ps.setDate(6, JdbcUtil.paraDate(posologia.getDataInicio()));
            ps.setDate(7, JdbcUtil.paraDate(posologia.getDataFim()));
            ps.setString(8, JdbcUtil.paraSimNao(posologia.isAtivo()));
            ps.setLong(9, posologia.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("atualizar posologia", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_posologia WHERE cd_posologia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("remover posologia", e);
        }
    }

    private int contar(String sql, Long id) {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("contar posologias", e);
        }
    }

    private Posologia montar(ResultSet rs) throws SQLException {
        Posologia posologia = new Posologia();
        posologia.setId(rs.getLong("cd_posologia"));
        posologia.setPacienteId(rs.getLong("cd_paciente"));
        posologia.setMedicamentoId(rs.getLong("cd_medicamento"));
        posologia.setPeriodo(PeriodoDia.valueOf(rs.getString("ds_periodo")));
        posologia.setQuantidadeDose(rs.getBigDecimal("qt_dose"));
        posologia.setTipoUso(TipoUso.valueOf(rs.getString("tp_uso")));
        posologia.setOrigem(OrigemMedicamento.valueOf(rs.getString("tp_origem")));
        posologia.setDataInicio(JdbcUtil.lerData(rs, "dt_inicio"));
        posologia.setDataFim(JdbcUtil.lerData(rs, "dt_fim"));
        posologia.setAtivo(JdbcUtil.lerSimNao(rs, "st_ativo"));
        return posologia;
    }
}
