/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.resourceindex;


import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Alberto
 */
public class ResourceIndexService {
    
    public static IResourceIndex getResourceIndexImpl() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        /*
        Configuration config = KConfiguration.getInstance().getConfiguration();
        String className = config.getString("resource.index.service.class");
        ClassLoader classLoader = ResourceIndexService.class.getClassLoader();
        return (IResourceIndex) classLoader.loadClass(className).newInstance();
        */
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        return injector.getInstance(IResourceIndex.class);
    }
}
