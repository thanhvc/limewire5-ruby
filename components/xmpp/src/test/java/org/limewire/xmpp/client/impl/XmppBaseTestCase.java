package org.limewire.xmpp.client.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.limewire.common.LimeWireCommonModule;
import org.limewire.concurrent.AbstractLazySingletonProvider;
import org.limewire.concurrent.ScheduledListeningExecutorService;
import org.limewire.concurrent.SimpleTimer;
import org.limewire.friend.api.LimeWireFriendModule;
import org.limewire.friend.impl.LimeWireFriendImplModule;
import org.limewire.http.auth.LimeWireHttpAuthModule;
import org.limewire.inject.AbstractModule;
import org.limewire.inject.GuiceUtils;
import org.limewire.lifecycle.ServiceRegistry;
import org.limewire.listener.ListenerSupport;
import org.limewire.net.LimeWireNetTestModule;
import org.limewire.net.address.AddressEvent;
import org.limewire.util.BaseTestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Base class for all tests xmpp 
 */
public abstract class XmppBaseTestCase extends BaseTestCase {

    protected static final String SERVICE = "gmail.com";
    protected static final int SLEEP = 5000; // Milliseconds

    private ServiceRegistry[] registries;
    protected XMPPConnectionFactoryImpl[] factories; 
    protected Injector[] injectors;


    public XmppBaseTestCase(String name) {
        super(name);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectors = new Injector[] { createInjector(getModules()), createInjector(getModules()) }; 
        registries = new ServiceRegistry[] { injectors[0].getInstance(ServiceRegistry.class), injectors[1].getInstance(ServiceRegistry.class) };
        for (ServiceRegistry registry : registries) {
            registry.initialize();
            registry.start();
        }
        factories = new XMPPConnectionFactoryImpl[] { injectors[0].getInstance(XMPPConnectionFactoryImpl.class), injectors[1].getInstance(XMPPConnectionFactoryImpl.class) };
    }

    protected Injector createInjector(Module... modules) {
        Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
        GuiceUtils.loadEagerSingletons(injector);
        return injector;
    }

    protected Module[] getModules() {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new LimeWireCommonModule());
        modules.add(new LimeWireHttpAuthModule());
        modules.add(new LimeWireFriendModule());
        modules.add(new LimeWireFriendImplModule());
        modules.addAll(getServiceModules());
        return modules.toArray(new Module[modules.size()]);
    }

    protected AddressEventTestBroadcaster getAddressBroadcaster(Injector injector) {
        return (AddressEventTestBroadcaster) injector.getInstance(Key.get(new TypeLiteral<ListenerSupport<AddressEvent>>(){}));
    }
    
    protected List<Module> getServiceModules() {
        Module xmppModule = new LimeWireXMPPTestModule();
        Module m = new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<ListenerSupport<AddressEvent>>(){}).toInstance(new AddressEventTestBroadcaster());
                bind(XMPPConnectionListenerMock.class);
                
                Annotation execAnn = Names.named("backgroundExecutor");
                Key<ScheduledListeningExecutorService> mainKey =
                        Key.get(ScheduledListeningExecutorService.class, execAnn);
                bind(mainKey).toProvider(BackgroundTimerProvider.class);
                bind(ScheduledExecutorService.class).annotatedWith(execAnn).to(mainKey);
                bind(Executor.class).annotatedWith(execAnn).to(mainKey);
                bind(ExecutorService.class).annotatedWith(execAnn).to(mainKey);
            }
        };
        return Arrays.asList(xmppModule, m, new LimeWireNetTestModule());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (XMPPConnectionFactoryImpl factory : factories) {
            factory.stop();
        }
        for (ServiceRegistry registry : registries) {
            registry.stop();
        }
        Thread.sleep(SLEEP);
    }
    
    @Singleton
    private static class BackgroundTimerProvider extends AbstractLazySingletonProvider<ScheduledListeningExecutorService> {
        @Override
        protected ScheduledListeningExecutorService createObject() {
            return new SimpleTimer(true);
        }
    }
}
