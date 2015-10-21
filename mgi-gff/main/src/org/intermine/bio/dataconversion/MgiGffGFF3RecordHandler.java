package org.intermine.bio.dataconversion;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.bio.io.gff3.GFF3Record;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;

/**
 * A converter/retriever for the MgiGff dataset via GFF files.
 */

public class MgiGffGFF3RecordHandler extends GFF3RecordHandler
{

    /**
     * Create a new MgiGffGFF3RecordHandler for the given data model.
     * @param model the model for which items will be created
     */
    public MgiGffGFF3RecordHandler (Model model) {
        super(model);
        refsAndCollections.put("Exon", "transcripts");
        refsAndCollections.put("FivePrimeUTR","MRNAs");
        refsAndCollections.put("MatchPart","genes");
        refsAndCollections.put("MRNA", "genes");
        refsAndCollections.put("StopCodon","MRNAs");
        refsAndCollections.put("StartCodon","MRNAs");
        refsAndCollections.put("PseudogeneicExon","pseudogenicTranscripts");
        refsAndCollections.put("ThreePrimeUTR","MRNAs");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(GFF3Record record) {
        // This method is called for every line of GFF3 file(s) being read.  Features and their
        // locations are already created but not stored so you can make changes here.  Attributes
        // are from the last column of the file are available in a map with the attribute name as
        // the key.   For example:
        //
        //     Item feature = getFeature();
        //     String symbol = record.getAttributes().get("symbol");
        //     feature.setAttrinte("symbol", symbol);
        //
        // Any new Items created can be stored by calling addItem().  For example:
        // 
        //     String geneIdentifier = record.getAttributes().get("gene");
        //     gene = createItem("Gene");
        //     gene.setAttribute("primaryIdentifier", geneIdentifier);
        //     addItem(gene);
        //
        // You should make sure that new Items you create are unique, i.e. by storing in a map by
        // some identifier. 

	Item feature = getFeature();
        String pi = feature.getAttribute("primaryIdentifier").getValue();
        pi = pi.replace("MGI:MGI:","MGI:");
        feature.setAttribute("primaryIdentifier",pi);
	System.out.println(pi);




    }

}
