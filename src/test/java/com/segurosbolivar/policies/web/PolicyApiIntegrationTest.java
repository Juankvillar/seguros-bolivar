package com.segurosbolivar.policies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PolicyApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsRequestsWithoutApiKey() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid x-api-key"));
    }

    @Test
    void listsPoliciesFilteredByTypeAndStatus() throws Exception {
        mockMvc.perform(get("/polizas")
                        .header("x-api-key", "123456")
                        .param("tipo", "COLECTIVA")
                        .param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("COLECTIVA"))
                .andExpect(jsonPath("$[0].status").value("ACTIVA"));
    }

    @Test
    void findsRisksByPolicyId() throws Exception {
        mockMvc.perform(get("/polizas/1/riesgos")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].status").value("ACTIVO"));
    }

    @Test
    void returnsNotFoundWhenQueryingRisksForNonExistentPolicy() throws Exception {
        mockMvc.perform(get("/polizas/999/riesgos")
                        .header("x-api-key", "123456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Policy with id 999 was not found"));
    }

    @Test
    void renewsPolicySuccessfullyIncreasingRentAndPremium() throws Exception {
        mockMvc.perform(post("/polizas/2/renovar")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("RENOVADA"))
                .andExpect(jsonPath("$.monthlyRent").value(1890000.00))
                .andExpect(jsonPath("$.premium").value(22680000.00));
    }

    @Test
    void cancelsPolicyAndCancelsAllItsAssociatedRisks() throws Exception {
        mockMvc.perform(post("/polizas/1/cancelar")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(get("/polizas/1/riesgos")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELADO"))
                .andExpect(jsonPath("$[1].status").value("CANCELADO"));
    }

    @Test
    void rejectsRenewalOfCancelledPolicy() throws Exception {
        mockMvc.perform(post("/polizas/2/cancelar")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/polizas/2/renovar")
                        .header("x-api-key", "123456"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A cancelled policy cannot be renewed"));
    }

    @Test
    void addsRiskToCollectivePolicy() throws Exception {
        mockMvc.perform(post("/polizas/1/riesgos")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Apartamento 103\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.description").value("Apartamento 103"));
    }

    @Test
    void rejectsAddingRiskToIndividualPolicy() throws Exception {
        mockMvc.perform(post("/polizas/2/riesgos")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Apartamento 502\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Risks can only be added to collective policies"));
    }

    @Test
    void cancelsSingleRiskSuccessfully() throws Exception {
        mockMvc.perform(post("/riesgos/101/cancelar")
                        .header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    void registersCoreMockEventSuccessfully() throws Exception {
        mockMvc.perform(post("/core-mock/evento")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evento").value("ACTUALIZACION"))
                .andExpect(jsonPath("$.polizaId").value(555))
                .andExpect(jsonPath("$.message").value("CORE update attempt registered"));
    }

    @Test
    void acceptsOnlyTheExpectedCoreUpdateEvent() throws Exception {
        mockMvc.perform(post("/core-mock/evento")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"CREACION\",\"polizaId\":555}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only ACTUALIZACION event is supported"));
    }
}
