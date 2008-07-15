/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.algorithms.scoring;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Joshua O'Madadhain
 */
public class TestPageRank extends TestCase {
	
	private Map<Integer,Number> edgeWeights;
	private DirectedGraph<Integer,Integer> graph;
	private Factory<Integer> edgeFactory;
	
    public static Test suite() {
        return new TestSuite(TestPageRank.class);
    }

    @Override
    protected void setUp() {
    	edgeWeights = new HashMap<Integer,Number>();
    	edgeFactory = new Factory<Integer>() {
    		int i=0;
			public Integer create() {
				return i++;
			}};
    }

    private void addEdge(Graph<Integer,Integer> G, Integer v1, Integer v2, double weight) {
    	Integer edge = edgeFactory.create();
    	graph.addEdge(edge, v1, v2);
    	edgeWeights.put(edge, weight);
    }

    public void testRanker() {
    	graph = new DirectedSparseMultigraph<Integer,Integer>();
    	for(int i=0; i<4; i++) {
    		graph.addVertex(i);
    	}
        addEdge(graph,0,1,1.0);
        addEdge(graph,1,2,1.0);
        addEdge(graph,2,3,0.5);
        addEdge(graph,3,1,1.0);
        addEdge(graph,2,1,0.5);

        PageRankWithPriors<Integer, Integer> pr = new PageRank<Integer, Integer>(graph, MapTransformer.getInstance(edgeWeights), 0);
        pr.evaluate();
        
        Transformer<Integer, Number> pr_scores = pr.getVertexScores();
        
        Assert.assertEquals(pr_scores.transform(0).doubleValue(), 0, pr.getTolerance());
        Assert.assertEquals(pr_scores.transform(1).doubleValue(), 0.4, pr.getTolerance());
        Assert.assertEquals(pr_scores.transform(2).doubleValue(), 0.4, pr.getTolerance());
        Assert.assertEquals(pr_scores.transform(3).doubleValue(), 0.2, pr.getTolerance());

//        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(0)).rankScore,0.4,.001));
//        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(1)).rankScore,0.4,.001));
//        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(2)).rankScore,0.2,.001));
//        Assert.assertTrue(NumericalPrecision.equal(((Ranking)ranker.getRankings().get(3)).rankScore,0,.001));
    }
}
