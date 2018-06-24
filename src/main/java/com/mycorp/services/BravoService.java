package com.mycorp.services;

/**
 * Created by pepec on 24/06/2018.
 */

import com.mycorp.support.DatosCliente;
import com.mycorp.support.ValueCode;
import com.mycorp.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import util.datos.UsuarioAlta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Service
public class BravoService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BravoService.class);

    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    /** The rest template. */
    @Autowired
    @Qualifier("restTemplateUTF8")
    private RestTemplate restTemplate;

    /** Inyectamos el servicio para obtener el id de cliente **/
    @Autowired
    ClientService clientService;

    public String generaDatos(UsuarioAlta usuarioAlta, StringBuilder clientName, StringBuilder datosServicio){
        
        StringBuilder datosBravo = new StringBuilder();
        
        try
        {
            // Obtenemos el id del cliente
            String idCliente = clientService.getIdCliente(usuarioAlta, clientName, datosServicio);
            
            datosBravo.append(Constants.ESCAPED_LINE_SEPARATOR + "Datos recuperados de BRAVO:" +
                    Constants.ESCAPED_LINE_SEPARATOR + Constants.ESCAPED_LINE_SEPARATOR);

            // Obtenemos los datos del cliente
            DatosCliente cliente = restTemplate.getForObject("http://localhost:8080/test-endpoint", DatosCliente.class, idCliente);

            datosBravo.append("TelÃ©fono: ").append(cliente.getGenTGrupoTmk()).append(Constants.ESCAPED_LINE_SEPARATOR);


            datosBravo.append("Feha de nacimiento: ").append(formatter.format(formatter.parse(cliente.getFechaNacimiento()))).append(Constants.ESCAPED_LINE_SEPARATOR);

            List< ValueCode > tiposDocumentos = getTiposDocumentosRegistro();
            for(int i = 0; i < tiposDocumentos.size();i++)
            {
                if(tiposDocumentos.get(i).getCode().equals(cliente.getGenCTipoDocumento().toString()))
                {
                    datosBravo.append("Tipo de documento: ").append(tiposDocumentos.get(i).getValue()).append(Constants.ESCAPED_LINE_SEPARATOR);
                }
            }
            datosBravo.append("NÃºmero documento: ").append(cliente.getNumeroDocAcred()).append(Constants.ESCAPED_LINE_SEPARATOR);

            datosBravo.append("Tipo cliente: ");
            switch (cliente.getGenTTipoCliente()) {
                case 1:
                    datosBravo.append("POTENCIAL").append(Constants.ESCAPED_LINE_SEPARATOR);
                    break;
                case 2:
                    datosBravo.append("REAL").append(Constants.ESCAPED_LINE_SEPARATOR);
                    break;
                case 3:
                    datosBravo.append("PROSPECTO").append(Constants.ESCAPED_LINE_SEPARATOR);
                    break;
            }

            datosBravo.append("ID estado del cliente: ").append(cliente.getGenTStatus()).append(Constants.ESCAPED_LINE_SEPARATOR);
            datosBravo.append("ID motivo de alta cliente: ").append(cliente.getIdMotivoAlta()).append(Constants.ESCAPED_LINE_SEPARATOR);
            datosBravo.append("Registrado: ").append((cliente.getfInactivoWeb() == null ? "SÃ­" : "No")).append(Constants.ESCAPED_LINE_SEPARATOR + Constants.ESCAPED_LINE_SEPARATOR);
        }catch(Exception e)
        {
            LOG.error("Error al obtener los datos en BRAVO del cliente", e);
        }

        return datosBravo.toString();
    }

    public List< ValueCode > getTiposDocumentosRegistro() {
        return Arrays.asList( new ValueCode(), new ValueCode() ); // simulacion servicio externo
    }
}
