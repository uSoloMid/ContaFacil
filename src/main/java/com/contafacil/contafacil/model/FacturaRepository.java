package com.contafacil.contafacil.repository;

import com.contafacil.contafacil.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, String> {
    // Aquí puedes añadir métodos personalizados si quieres
}
