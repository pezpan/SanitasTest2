package com.mycorp;

import com.mycorp.beans.Zendesk;
import com.mycorp.services.BravoService;
import com.mycorp.services.ZendeskService;
import com.mycorp.support.MensajeriaService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * Unit test for simple App.
 */
@RunWith(MockitoJUnitRunner.class)
public class RealizarSimulacionTest extends TestCase {

    @Mock
    MensajeriaService mensajeriaService;

    @Mock
    BravoService bravoService;

    @Mock
    Zendesk zendesk;

    @InjectMocks
    ZendeskService zendeskService;

    @Before
    public void init(){
        // Inicializamos los mocks
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Rigourous Test :-)
     */
    @Test
    public void testApp() {
        assertTrue( true );
    }

}
