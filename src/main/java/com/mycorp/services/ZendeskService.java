package com.mycorp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.beans.Zendesk;
import com.mycorp.support.CorreoElectronico;
import com.mycorp.support.MensajeriaService;
import com.mycorp.support.Ticket;
import com.mycorp.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import util.datos.UsuarioAlta;

@Service
public class ZendeskService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger( ZendeskService.class );

    @Value("#{envPC['zendesk.ticket']}")
    public String PETICION_ZENDESK= "";



    @Value("#{envPC['tarjetas.getDatos']}")
    public String TARJETAS_GETDATOS = "";

    @Value("#{envPC['cliente.getDatos']}")
    public String CLIENTE_GETDATOS = "";

    @Value("#{envPC['zendesk.error.mail.funcionalidad']}")
    public String ZENDESK_ERROR_MAIL_FUNCIONALIDAD = "";

    @Value("#{envPC['zendesk.error.destinatario']}")
    public String ZENDESK_ERROR_DESTINATARIO = "";

    @Autowired
    @Qualifier( "emailService" )
    MensajeriaService emailService;

    // Servicio bravo
    @Autowired
    BravoService bravoService;

    //Añadimos nuestro bean para el servicio zendesk
    @Autowired
    Zendesk zendesk;

    /**
     * Crea un ticket en Zendesk. Si se ha informado el nÂº de tarjeta, obtiene los datos asociados a dicha tarjeta de un servicio externo.
     * @param usuarioAlta
     * @param userAgent
     */
    public String altaTicketZendesk(UsuarioAlta usuarioAlta, String userAgent){

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        StringBuilder clientName = new StringBuilder();
        StringBuilder datosServicio = new StringBuilder();

        // Obtenemos el ticket haciendo uso de nuestros servicios
        String datosBravo = bravoService.generaDatos(usuarioAlta, clientName, datosServicio);
        String datosUsuario = getDatosUsuario(usuarioAlta, userAgent);

        String ticket = String.format(PETICION_ZENDESK, clientName.toString(), usuarioAlta.getEmail(),
                datosUsuario + datosBravo + parseJsonBravo(datosServicio));
        ticket = ticket.replaceAll("[" + Constants.ESCAPED_LINE_SEPARATOR + "]", " ");

        try{
            //Ticket
            Ticket petiZendesk = mapper.readValue(ticket, Ticket.class);
            zendesk.createTicket(petiZendesk);

        }catch(Exception e){
            LOG.error("Error al crear ticket ZENDESK", e);
            // Send email

            CorreoElectronico correo = new CorreoElectronico( Long.parseLong(ZENDESK_ERROR_MAIL_FUNCIONALIDAD), "es" )
                    .addParam(datosUsuario.toString().replaceAll(Constants.ESCAPE_ER + Constants.ESCAPED_LINE_SEPARATOR, Constants.HTML_BR))
                    .addParam(datosBravo.toString().replaceAll(Constants.ESCAPE_ER + Constants.ESCAPED_LINE_SEPARATOR, Constants.HTML_BR));
            correo.setEmailA( ZENDESK_ERROR_DESTINATARIO );
            try
            {
                emailService.enviar( correo );
            }catch(Exception ex){
                LOG.error("Error al enviar mail", ex);
            }

        }

        return (datosUsuario + datosBravo.toString());
    }

    public String getDatosUsuario(UsuarioAlta usuarioAlta, String userAgent){
        StringBuilder datosUsuario = new StringBuilder();

        // AÃ±ade los datos del formulario
        if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
            datosUsuario.append("NÂº de poliza/colectivo: ").append(usuarioAlta.getNumPoliza()).append("/").append(usuarioAlta.getNumDocAcreditativo()).append(Constants.ESCAPED_LINE_SEPARATOR);
        }else{
            datosUsuario.append("NÂº tarjeta Sanitas o Identificador: ").append(usuarioAlta.getNumTarjeta()).append(Constants.ESCAPED_LINE_SEPARATOR);
        }
        datosUsuario.append("Tipo documento: ").append(usuarioAlta.getTipoDocAcreditativo()).append(Constants.ESCAPED_LINE_SEPARATOR);
        datosUsuario.append("NÂº documento: ").append(usuarioAlta.getNumDocAcreditativo()).append(Constants.ESCAPED_LINE_SEPARATOR);
        datosUsuario.append("Email personal: ").append(usuarioAlta.getEmail()).append(Constants.ESCAPED_LINE_SEPARATOR);
        datosUsuario.append("NÂº mÃ³vil: ").append(usuarioAlta.getNumeroTelefono()).append(Constants.ESCAPED_LINE_SEPARATOR);
        datosUsuario.append("User Agent: ").append(userAgent).append(Constants.ESCAPED_LINE_SEPARATOR);

        return datosUsuario.toString();
    }

    /**
     * MÃ©todo para parsear el JSON de respuesta de los servicios de tarjeta/pÃ³liza
     *
     * @param resBravo
     * @return
     */
    private String parseJsonBravo(StringBuilder resBravo)
    {
        return resBravo.toString().replaceAll("[\\[\\]\\{\\}\\\"\\r]", "")
                .replaceAll(Constants.ESCAPED_LINE_SEPARATOR,Constants. ESCAPE_ER + Constants.ESCAPED_LINE_SEPARATOR);
    }
}