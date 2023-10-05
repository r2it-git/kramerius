package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.*;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.utils.LicensesCriteriaList;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.w3c.dom.Document;

import com.maxmind.geoip2.DatabaseReader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CriteriaDNNTUtils {
    
    public static ThreadLocal<RightsReturnObject> currentThreadReturnObject = new ThreadLocal<>();

    public  static Logger LOGGER = Logger.getLogger(CriteriaDNNTUtils.class.getName());
    
    


    // check dnnt flag from solr
    public static EvaluatingResultState checkDnnt(RightCriteriumContext ctx) {
        try {
            SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
            String pid = ctx.getRequestedPid();
            Document doc = solrAccess.getSolrDataByPid(pid);
            String val = SolrUtils.disectDNNTFlag(doc.getDocumentElement());
            return (val !=  null && val.equals("true")) ? EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    // allowed by license
    public static boolean allowedByReadLicenseRight(RightsReturnObject obj, License license) {
        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null) {
            if (LicensesCriteriaList.NAMES.contains(obj.getRight().getCriteriumWrapper().getRightCriterium().getQName())){

                String providedByLicense = obj.getEvaluateInfoMap().get(ReadDNNTLabels.PROVIDED_BY_DNNT_LICENSE);
                if (providedByLicense == null) {
                    providedByLicense = obj.getEvaluateInfoMap().get(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL);
                }

                return license != null && license.getName() != null && providedByLicense != null && providedByLicense.equals(license.getName());
            }
        }
        return false;
    }


    
    public static boolean allowedByReadDNNTFlagRight(RightsReturnObject obj) {
        List<String> dnntLicenses = Arrays.asList("dnnto","dnntt");
        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null) {
            if (   LicensesCriteriaList.NAMES.contains(obj.getRight().getCriteriumWrapper().getRightCriterium().getQName())) {
                License license = obj.getRight().getCriteriumWrapper().getLicense();
                return dnntLicenses.contains(license.getName());
            }
        }
        return false;
    }



//    public static String getReadDNNTLabel(RightsReturnObject obj) {
//        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null) {
//            if (obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTLabels.class.getName()) ||
//                    obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTLabelsIPFiltered.class.getName())
//                    ) {
//                return obj.getRight().getCriteriumWrapper().getCriteriumParams().getObjects();
//            }
//        }
//        return null;
//
//    }



    public static void checkContainsCriterium(RightCriteriumContext ctx, RightsManager manager, Class ... clzs) throws CriteriaPrecoditionException {
        String[] pids = new String[] {SpecialObjects.REPOSITORY.getPid()};
        Right[] rights = manager.findRights(pids, SecuredActions.A_PDF_READ.getFormalName(), ctx.getUser());
        for (Right r : rights) {
            if (r == null) continue;
            if (r.getCriteriumWrapper() == null) continue;
            RightCriterium rightCriterium = r.getCriteriumWrapper().getRightCriterium();
            String qName = rightCriterium.getQName();
            for (Class clz:clzs) {
                if (qName.equals(clz.getName())) {
                    return;
                }
            }
        }
        List<String> collections = Arrays.stream(clzs).map(s -> s.getName()).collect(Collectors.toList());
        throw new CriteriaPrecoditionException("These flags are not set : "+collections);
    }


    public static boolean matchLicense(Document solrDoc, License license) {
        List<String> indexedLabels = SolrUtils.disectLicenses(solrDoc.getDocumentElement());
        if (indexedLabels != null && license != null) {
            String labelName = license.getName();
            if (indexedLabels.contains(labelName)) return true;
        }
        return false;
    }
}