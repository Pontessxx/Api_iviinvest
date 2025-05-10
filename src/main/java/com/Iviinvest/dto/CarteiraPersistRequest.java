// src/main/java/com/Iviinvest/dto/CarteiraPersistRequest.java
package com.Iviinvest.dto;

import java.util.List;
import java.util.Map;

public class CarteiraPersistRequest {
    private Map<String,Integer> percentuais;
    private Map<String, List<String>> ativos;

    public Map<String,Integer> getPercentuais() { return percentuais; }
    public void setPercentuais(Map<String,Integer> percentuais) { this.percentuais = percentuais; }

    public Map<String,List<String>> getAtivos() { return ativos; }
    public void setAtivos(Map<String,List<String>> ativos) { this.ativos = ativos; }
}
