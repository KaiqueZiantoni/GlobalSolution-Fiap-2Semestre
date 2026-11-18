package br.com.fiap.tomeidoutor.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Apoio comum aos DAOs: leitura de sequence e conversao de datas
 * e de nulos entre Java e JDBC.
 */
final class JdbcUtil {

    private JdbcUtil() {
    }

    /** Pede o proximo valor de uma sequence (sq_tb_*). O nome vem sempre de constante do DAO. */
    static long proximoId(Connection conexao, String sequence) throws SQLException {
        String sql = "SELECT " + sequence + ".NEXTVAL FROM dual";
        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    static Date paraDate(LocalDate data) {
        return data == null ? null : Date.valueOf(data);
    }

    static Timestamp paraTimestamp(LocalDateTime dataHora) {
        return dataHora == null ? null : Timestamp.valueOf(dataHora);
    }

    static LocalDate lerData(ResultSet rs, String coluna) throws SQLException {
        Timestamp valor = rs.getTimestamp(coluna);
        return valor == null ? null : valor.toLocalDateTime().toLocalDate();
    }

    static LocalDateTime lerDataHora(ResultSet rs, String coluna) throws SQLException {
        Timestamp valor = rs.getTimestamp(coluna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    /** Colunas st_* guardam 'S' ou 'N'. */
    static String paraSimNao(boolean valor) {
        return valor ? "S" : "N";
    }

    static boolean lerSimNao(ResultSet rs, String coluna) throws SQLException {
        return "S".equals(rs.getString(coluna));
    }
}
