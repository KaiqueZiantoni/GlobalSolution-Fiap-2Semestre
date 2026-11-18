package br.com.fiap.tomeidoutor.dao;

import br.com.fiap.tomeidoutor.exception.AcessoDadosException;
import br.com.fiap.tomeidoutor.model.AutorRegistro;
import br.com.fiap.tomeidoutor.model.RegistroDose;
import br.com.fiap.tomeidoutor.model.StatusDose;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tb_registro_dose com JDBC puro.
 * A coluna cd_posologia e a chave estrangeira do 1:N posologia -> registro de dose.
 */
@Repository
public class RegistroDoseDao {

    private static final String SEQUENCE = "sq_tb_registro_dose";
    private static final String COLUNAS =
            "cd_registro, cd_posologia, dt_prevista, dt_confirmacao, tp_status, tp_autor";

    private final DataSource dataSource;

    public RegistroDoseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava o registro com o proximo id da sequence e devolve o mesmo objeto, ja com o id. */
    public RegistroDose inserir(RegistroDose registro) {
        String sql = "INSERT INTO tb_registro_dose (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conexao, SEQUENCE);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setLong(2, registro.getPosologiaId());
                ps.setTimestamp(3, JdbcUtil.paraTimestamp(registro.getDataPrevista()));
                ps.setTimestamp(4, JdbcUtil.paraTimestamp(registro.getDataConfirmacao()));
                ps.setString(5, registro.getStatus().name());
                ps.setString(6, nomeOuNulo(registro.getAutor()));
                ps.executeUpdate();
            }
            registro.setId(id);
            return registro;
        } catch (SQLException e) {
            throw new AcessoDadosException("cadastrar registro de dose", e);
        }
    }

    /** Lado N do 1:N: todos os registros de dose de uma posologia. */
    public List<RegistroDose> listarPorPosologia(Long posologiaId) {
        String sql = "SELECT " + COLUNAS + " FROM tb_registro_dose WHERE cd_posologia = ? "
                + "ORDER BY dt_prevista, cd_registro";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, posologiaId);
            try (ResultSet rs = ps.executeQuery()) {
                List<RegistroDose> registros = new ArrayList<>();
                while (rs.next()) {
                    registros.add(montar(rs));
                }
                return registros;
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("listar registros de dose da posologia", e);
        }
    }

    /** Devolve null quando o id nao existe. */
    public RegistroDose buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_registro_dose WHERE cd_registro = ?";
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
            throw new AcessoDadosException("buscar registro de dose", e);
        }
    }

    /**
     * Diz se a posologia ja tem uma dose prevista para o mesmo dia e hora.
     *
     * @param ignorarId id do proprio registro na atualizacao; nulo no cadastro
     */
    public boolean existeDosePrevista(Long posologiaId, LocalDateTime dataPrevista, Long ignorarId) {
        String sql = "SELECT 1 FROM tb_registro_dose WHERE cd_posologia = ? AND dt_prevista = ? "
                + "AND cd_registro <> ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, posologiaId);
            ps.setTimestamp(2, JdbcUtil.paraTimestamp(dataPrevista));
            ps.setLong(3, ignorarId == null ? -1L : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar dose prevista", e);
        }
    }

    public int contarPorPosologia(Long posologiaId) {
        String sql = "SELECT COUNT(*) FROM tb_registro_dose WHERE cd_posologia = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, posologiaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("contar registros de dose", e);
        }
    }

    /** Devolve false quando o id nao existe. A posologia do registro nao muda. */
    public boolean atualizar(RegistroDose registro) {
        String sql = "UPDATE tb_registro_dose SET dt_prevista = ?, dt_confirmacao = ?, tp_status = ?, "
                + "tp_autor = ? WHERE cd_registro = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setTimestamp(1, JdbcUtil.paraTimestamp(registro.getDataPrevista()));
            ps.setTimestamp(2, JdbcUtil.paraTimestamp(registro.getDataConfirmacao()));
            ps.setString(3, registro.getStatus().name());
            ps.setString(4, nomeOuNulo(registro.getAutor()));
            ps.setLong(5, registro.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("atualizar registro de dose", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_registro_dose WHERE cd_registro = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("remover registro de dose", e);
        }
    }

    private static String nomeOuNulo(AutorRegistro autor) {
        return autor == null ? null : autor.name();
    }

    private RegistroDose montar(ResultSet rs) throws SQLException {
        RegistroDose registro = new RegistroDose();
        registro.setId(rs.getLong("cd_registro"));
        registro.setPosologiaId(rs.getLong("cd_posologia"));
        registro.setDataPrevista(JdbcUtil.lerDataHora(rs, "dt_prevista"));
        registro.setDataConfirmacao(JdbcUtil.lerDataHora(rs, "dt_confirmacao"));
        registro.setStatus(StatusDose.valueOf(rs.getString("tp_status")));
        String autor = rs.getString("tp_autor");
        registro.setAutor(autor == null ? null : AutorRegistro.valueOf(autor));
        return registro;
    }
}
