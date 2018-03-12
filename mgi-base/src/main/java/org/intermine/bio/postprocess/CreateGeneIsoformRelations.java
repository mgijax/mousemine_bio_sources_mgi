package org.intermine.bio.postprocess;

/*
 * Copyright (C) 2002-2015 FlyMine
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
import org.intermine.bio.util.Constants;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.FieldDescriptor;
import org.intermine.model.InterMineObject;
import org.intermine.model.FastPathObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.QueryReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;

/**
 * Create the relationships between genes and isoforms of their cannonical protiens
 * @author Steve Neuhauser
 */
public class CreateGeneIsoformRelations extends PostProcessor

{
    private static final Logger LOG = Logger.getLogger(CreateGeneIsoformRelations.class);

    /**
     * Construct with an ObjectStoreWriter, read and write from same ObjectStore
     * @param osw an ObjectStore to write to
     */
    public CreateGeneIsoformRelations(ObjectStoreWriter osw) {
        super(osw);
    }

    public void postProcess() throws ObjectStoreException {
      LOG.info("Creating gene to isoform relationships");
      String pi = "primaryIdentifier";

      try{
        Iterator<ResultsRow<InterMineObject>> it = doQuery();

        InterMineObject gene = null;
        Set<InterMineObject> proteins = null;

        osw.beginTransaction();
        while(it.hasNext()){
          ResultsRow<InterMineObject> rr = it.next();
          if(gene == null ||
               !gene.getFieldValue(pi).equals(rr.get(1).getFieldValue(pi))){
            if(gene != null){
              gene.setFieldValue("proteins",proteins);
              osw.store(gene);
            }
            gene = rr.get(1);
            proteins = new HashSet<InterMineObject>();

            proteins.addAll((Set<InterMineObject>)gene.getFieldValue("proteins"));
          }

         InterMineObject isoform = rr.get(0);
         proteins.add(isoform);
       
        }
	if(gene != null){
	    gene.setFieldValue("proteins",proteins);
	    osw.store(gene);
	}
        osw.commitTransaction();
      }catch(Exception e){
        LOG.error("Failed to create gene to isoform associations");
        LOG.error(e);
        e.printStackTrace();
      }
    }


private Iterator<ResultsRow<InterMineObject>> doQuery()
        throws ObjectStoreException, IllegalAccessException {

        ObjectStore os = this.osw.getObjectStore();

        Class proteinCls = os.getModel().getClassDescriptorByName("Protein").getType();
        Class geneCls = os.getModel().getClassDescriptorByName("Gene").getType();
        

        Query q = new Query();

        q.setDistinct(false);
        QueryClass qcSource = new QueryClass(proteinCls);
        q.addFrom(qcSource);
        q.addToSelect(qcSource);
           
        QueryClass qcConnecting = new QueryClass(proteinCls);
        q.addFrom(qcConnecting);

        QueryClass qcDest = new QueryClass(geneCls);
        q.addFrom(qcDest);
        q.addToSelect(qcDest);
        q.addToOrderBy(qcDest);
        
        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);
        QueryObjectReference ref1 =
            new QueryObjectReference(qcSource, "canonicalProtein");
        ContainsConstraint cc1 = new ContainsConstraint(ref1, ConstraintOp.CONTAINS, qcConnecting);
        cs.addConstraint(cc1);
        QueryReference ref2;

        ref2 = new QueryCollectionReference(qcConnecting, "genes");
        
        ContainsConstraint cc2 = new ContainsConstraint(ref2, ConstraintOp.CONTAINS, qcDest);
        cs.addConstraint(cc2);
        q.setConstraint(cs);

        ((ObjectStoreInterMineImpl) os).precompute(q, Constants
                                                   .PRECOMPUTE_CATEGORY);
        Results res = os.execute(q, 5000, true, true, true);

        @SuppressWarnings("unchecked") Iterator<ResultsRow<InterMineObject>> retval = (Iterator) res
            .iterator();
        return retval;
    }

}

