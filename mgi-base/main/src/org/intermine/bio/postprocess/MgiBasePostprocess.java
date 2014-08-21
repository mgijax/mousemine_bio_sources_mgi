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
import org.intermine.model.bio.GOAnnotation;
import org.intermine.model.bio.SequenceFeature;
import org.intermine.model.bio.Gene;
import org.intermine.bio.util.Constants;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.intermine.ObjectStoreInterMineImpl;
import org.intermine.objectstore.query.ConstraintOp;
// package for ConstraintOp on beta branch 
//import org.intermine.metadata.ConstraintOp;
import org.intermine.objectstore.query.ConstraintSet;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryCollectionReference;
import org.intermine.objectstore.query.QueryObjectReference;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.postprocess.PostProcessor;

/**
 * Populates the goAnnotation collection for SequenceFeatures.
 *
 * @author Joel Richardson
 */
public class MgiBasePostprocess extends PostProcessor
{
    private static final Logger LOG = Logger.getLogger(MgiBasePostprocess.class);
    protected ObjectStore os;

    /**
     * Create a new UpdateOrthologes object from an ObjectStoreWriter
     * @param osw writer on genomic ObjectStore
     */
    public MgiBasePostprocess(ObjectStoreWriter osw) {
        super(osw);
        this.os = osw.getObjectStore();
    }


    /**
     * Populates the goAnnotation collection for SequenceFeatures.
     * @throws ObjectStoreException if anything goes wrong
     */
    public void postProcess()
        throws ObjectStoreException {

        long startTime = System.currentTimeMillis();

        osw.beginTransaction();

        Iterator<?> resIter = findGoSubjects();

        int count = 0;
        Gene lastFeature = null;
        Set<GOAnnotation> goCollection = new HashSet<GOAnnotation>();

        while (resIter.hasNext()) {
            ResultsRow<?> rr = (ResultsRow<?>) resIter.next();
            GOAnnotation thisAnnotation = (GOAnnotation) rr.get(0);
            Gene thisFeature = (Gene) rr.get(1);
            if (lastFeature != null && !(lastFeature.equals(thisFeature))) {
                lastFeature.setGoAnnotation(goCollection);
                LOG.debug("store feature " + lastFeature.getPrimaryIdentifier() + " with "
                        + lastFeature.getGoAnnotation().size() + " GO annotations.");
                osw.store(lastFeature);

                lastFeature = thisFeature;
                goCollection = new HashSet<GOAnnotation>();
            }
            goCollection.add(thisAnnotation);
            lastFeature = thisFeature;
            count++;
        }

        if (lastFeature != null) {
            lastFeature.setGoAnnotation(goCollection);
            LOG.debug("store feature " + lastFeature.getPrimaryIdentifier() + " with "
                    + lastFeature.getGoAnnotation().size() + " GO annotations.");
            osw.store(lastFeature);
        }

        LOG.info("Created " + count + " new GOAnnotation objects for Features"
                + " - took " + (System.currentTimeMillis() - startTime) + " ms.");
        osw.commitTransaction();
    }


    /**
     * Query GOAnnotation->Gene, return iterator over annotation and gene.
     *
     */
    private Iterator<?> findGoSubjects() throws ObjectStoreException {
        Query q = new Query();

        q.setDistinct(false);

        QueryClass qcAnnotation = new QueryClass(GOAnnotation.class);
        q.addFrom(qcAnnotation);
        q.addToSelect(qcAnnotation);

        QueryClass qcFeature = new QueryClass(Gene.class);
        q.addFrom(qcFeature);
        q.addToSelect(qcFeature);
        q.addToOrderBy(qcFeature);

        ConstraintSet cs = new ConstraintSet(ConstraintOp.AND);

        QueryObjectReference annSubjectRef =
            new QueryObjectReference(qcAnnotation, "subject");
        cs.addConstraint(new ContainsConstraint(annSubjectRef, ConstraintOp.CONTAINS, qcFeature));

        q.setConstraint(cs);

        ((ObjectStoreInterMineImpl) os).precompute(q, Constants.PRECOMPUTE_CATEGORY);
        Results res = os.execute(q, 5000, true, true, true);
        return res.iterator();
    }
}
