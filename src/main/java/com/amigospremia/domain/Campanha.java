package com.amigospremia.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "campanhas")
public class Campanha implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String titulo;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String descricao;
	
	@Column(nullable = false)
	private String imagem;
	
	@Column(nullable = false)
	private LocalDateTime dataHoraSorteio;
	
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal valorCota;
	
	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario criador;
	
	@Column(nullable = false)
	private Boolean ativa = true;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Campanha() {
	}

	public Campanha(String titulo, String descricao, String imagem, LocalDateTime dataHoraSorteio,
			BigDecimal valorCota, Usuario criador) {
		this.titulo = titulo;
		this.descricao = descricao;
		this.imagem = imagem;
		this.dataHoraSorteio = dataHoraSorteio;
		this.valorCota = valorCota;
		this.criador = criador;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public LocalDateTime getDataHoraSorteio() {
		return dataHoraSorteio;
	}

	public void setDataHoraSorteio(LocalDateTime dataHoraSorteio) {
		this.dataHoraSorteio = dataHoraSorteio;
	}

	public BigDecimal getValorCota() {
		return valorCota;
	}

	public void setValorCota(BigDecimal valorCota) {
		this.valorCota = valorCota;
	}

	public Usuario getCriador() {
		return criador;
	}

	public void setCriador(Usuario criador) {
		this.criador = criador;
	}

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
