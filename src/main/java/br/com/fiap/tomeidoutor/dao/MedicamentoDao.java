package br.com.fiap.tomeidoutor.dao;

import br.com.fiap.tomeidoutor.exception.AcessoDadosException;
import br.com.fiap.tomeidoutor.model.FormaMedicamento;
import br.com.fiap.tomeidoutor.model.Medicamento;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tb_medicamento com JDBC puro.
 */
@Repository
public class MedicamentoDao {

    private static final String SEQUENCE = "sq_tb_medicamento";
    private static final String COLUNAS =
            "cd_medicamento, nm_medicamento, nm_principio_ativo, ds_classe, nr_ean, tp_forma";

    private final DataSource dataSource;

    public MedicamentoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Grava o medicamento com o proximo id da sequence e devolve o mesmo objeto, ja com o id. */
    public Medicamento inserir(Medicamento medicamento) {
        String sql = "INSERT INTO tb_medicamento (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            long id = JdbcUtil.proximoId(conexao, SEQUENCE);
            try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, medicamento.getNome());
                ps.setString(3, medicamento.getPrincipioAtivo());
                ps.setString(4, medicamento.getClasse());
                ps.setString(5, medicamento.getEan());
                ps.setString(6, medicamento.getForma().name());
                ps.executeUpdate();
            }
            medicamento.setId(id);
            return medicamento;
        } catch (SQLException e) {
            throw new AcessoDadosException("cadastrar medicamento", e);
        }
    }

    public List<Medicamento> listar() {
        String sql = "SELECT " + COLUNAS + " FROM tb_medicamento ORDER BY cd_medicamento";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Medicamento> medicamentos = new ArrayList<>();
            while (rs.next()) {
                medicamentos.add(montar(rs));
            }
            return medicamentos;
        } catch (SQLException e) {
            throw new AcessoDadosException("listar medicamentos", e);
        }
    }

    /** Devolve null quando o id nao existe. */
    public Medicamento buscarPorId(Long id) {
        String sql = "SELECT " + COLUNAS + " FROM tb_medicamento WHERE cd_medicamento = ?";
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
            throw new AcessoDadosException("buscar medicamento", e);
        }
    }

    public boolean existe(Long id) {
        String sql = "SELECT 1 FROM tb_medicamento WHERE cd_medicamento = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar medicamento", e);
        }
    }

    /**
     * Diz se o codigo de barras ja pertence a outro medicamento.
     *
     * @param ignorarId id do proprio medicamento na atualizacao; nulo no cadastro
     */
    public boolean existeEan(String ean, Long ignorarId) {
        String sql = "SELECT 1 FROM tb_medicamento WHERE nr_ean = ? AND cd_medicamento <> ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, ean);
            ps.setLong(2, ignorarId == null ? -1L : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AcessoDadosException("verificar código de barras", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean atualizar(Medicamento medicamento) {
        String sql = "UPDATE tb_medicamento SET nm_medicamento = ?, nm_principio_ativo = ?, ds_classe = ?, "
                + "nr_ean = ?, tp_forma = ? WHERE cd_medicamento = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, medicamento.getNome());
            ps.setString(2, medicamento.getPrincipioAtivo());
            ps.setString(3, medicamento.getClasse());
            ps.setString(4, medicamento.getEan());
            ps.setString(5, medicamento.getForma().name());
            ps.setLong(6, medicamento.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("atualizar medicamento", e);
        }
    }

    /** Devolve false quando o id nao existe. */
    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_medicamento WHERE cd_medicamento = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AcessoDadosException("remover medicamento", e);
        }
    }

    private Medicamento montar(ResultSet rs) throws SQLException {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(rs.getLong("cd_medicamento"));
        medicamento.setNome(rs.getString("nm_medicamento"));
        medicamento.setPrincipioAtivo(rs.getString("nm_principio_ativo"));
        medicamento.setClasse(rs.getString("ds_classe"));
        medicamento.setEan(rs.getString("nr_ean"));
        medicamento.setForma(FormaMedicamento.valueOf(rs.getString("tp_forma")));
        return medicamento;
    }
}
