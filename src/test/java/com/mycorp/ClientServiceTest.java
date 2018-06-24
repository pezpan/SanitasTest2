package com.mycorp;

import com.mycorp.services.ClientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import portalclientesweb.ejb.interfaces.PortalClientesWebEJBRemote;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by pepec on 24/06/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    PortalClientesWebEJBRemote portalclientesWebEJBRemote;

    @InjectMocks
    ClientService clientService;

    @Before
    public void init(){
        // Inicializamos los mocks
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testService(){
        assertTrue( true );
    }
}
