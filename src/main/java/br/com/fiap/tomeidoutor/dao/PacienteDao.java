package br.com.fiap.tomeidoutor.dao;

import br.com.fiap.tomeidoutor.exception.AcessoDadosException;
import br.com.fiap.tomeidoutor.model.Paciente;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tb_paciente com JDBC puro.
 */
@Repository
public class PacienteDao {

    private static final String SEQUENCE = "sq_tb_paciente";
    private static final String COLUNAS = "cd_paciente, nm_paciente, dt_nascimento, nr_cep, nr_telefone";

    private final DataSource dataSource;

    public PacienteDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava o paciente com o proximo id da sequence e devolve o mesmo objeto, ja com o id. */
    public Paciente inserir(Paciente paciente) {
        String sql = "INSERT INTO tb_paciente (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conexao, SEQUENCE);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, paciente.getNome());
                ps.setDate(3, JdbcUtil.paraDate(paciente.getDataNascimento()));
                ps.setString(4, paciente.getCep());
                ps.setString(5, paciente.getTelefone());
                ps.executeUpdate();
            }
            paciente.setId(id);
            return paciente;
        } catch (SQLException e) {
            throw new AcessoDadosException("cadastrar paciente", e);
        }
    }

    public List<Paciente> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_paciente ORDER BY cd_paciente";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Paciente> pacientes = new ArrayList<>();
            while (rs.next()) {
                pacientes.add(montar(rs));
            }
            return pacientes;
        } catch (SQLException e) {
            throw new AcessoDadosException("listar pacientes", e);
        }
    }

    /** Devolve null quando o id nao existe. */
    public Paciente buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_paciente WHERE cd_paciente = ?";
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
            throw new AcessoDadosException("buscar paciente", e);
        }
    }

    public boolean existe(Long id) {
        String sql = "SELECT 1 FROM tb_paciente WHERE cd_paciente = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar paciente", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean atualizar(Paciente paciente) {
        String sql = "UPDATE tb_paciente SET nm_paciente = ?, dt_nascimento = ?, nr_cep = ?, nr_telefone = ? "
                + "WHERE cd_paciente = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, paciente.getNome());
            ps.setDate(2, JdbcUtil.paraDate(paciente.getDataNascimento()));
            ps.setString(3, paciente.getCep());
            ps.setString(4, paciente.getTelefone());
            ps.setLong(5, paciente.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("atualizar paciente", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_paciente WHERE cd_paciente = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("remover paciente", e);
        }
    }

    /**
     * Conta os filhos do paciente nas tabelas que ficam fora da API
     * (vinculos com cuidador e alertas de clima). As posologias sao contadas
     * pelo PosologiaDao.
     */
    public int contarVinculosEAlertas(Long id) {
        String sql = "SELECT (SELECT COUNT(*) FROM tb_vinculo_cuidador WHERE cd_paciente = ?) "
                + "+ (SELECT COUNT(*) FROM tb_alerta WHERE cd_paciente = ?) FROM dual";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar dependentes do paciente", e);
        }
    }

    private Paciente montar(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        paciente.setId(rs.getLong("cd_paciente"));
        paciente.setNome(rs.getString("nm_paciente"));
        paciente.setDataNascimento(JdbcUtil.lerData(rs, "dt_nascimento"));
        paciente.setCep(rs.getString("nr_cep"));
        paciente.setTelefone(rs.getString("nr_telefone"));
        return paciente;
    }
}
