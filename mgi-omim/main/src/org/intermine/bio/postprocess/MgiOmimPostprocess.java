package org.intermine.bio.postprocess;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.model.bio.OMIMTerm;
import org.intermine.bio.util.Constants;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;

/**
 * Populates the OMIMId field for OMIM terms contains just the numeric ID not 'OMIM:'
 *
 * @author Steve Neuhauser
 */
public class MgiOmimPostprocess extends PostProcessor
{
    private static final Logger LOG = Logger.getLogger(MgiOmimPostprocess.class);
    protected ObjectStore os;

    /**
     * Create a new class with an ObjectStoreWriter
     * @param osw writer on genomic ObjectStore
     */
    public MgiOmimPostprocess(ObjectStoreWriter osw) {
        super(osw);
        this.os = osw.getObjectStore();
    }


    /**
     * Populates the goAnnotation collection for SequenceFeatures.
     * @throws ObjectStoreException if anything goes wrong
     */
    public void postProcess()
        throws ObjectStoreException {

        LOG.info("MEDIC PostProcessor created");

        Query q = new Query();
        q.setDistinct(false);
        
        QueryClass qc = new QueryClass(OMIMTerm.class);
        q.addFrom(qc);
        q.addToSelect(qc); 
        ((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
        Results res = os.execute(q, 5000, true, true, true);

       osw.beginTransaction();
       LOG.info("MEDIC PostProcessor begining iteration");
       Iterator<?> iter = res.iterator();
       int count =0;
       try{
        while(iter.hasNext()){
            ResultsRow<?> rr = (ResultsRow<?>) iter.next();
            OMIMTerm term = (OMIMTerm) rr.get(0);
            String id = term .getIdentifier();
            if(id != null && id.indexOf("OMIM:") != -1){
             LOG.info(id+"-POW--->"+id.substring(5));
             id = id.substring(5);
             term.setoMIMId(new Integer(id));
             osw.store(term);
            }
        }
       osw.commitTransaction();
      }catch(Exception e){
	LOG.error(e);
      }
      LOG.info("MEDIC Postprocessor done");

    }


}
