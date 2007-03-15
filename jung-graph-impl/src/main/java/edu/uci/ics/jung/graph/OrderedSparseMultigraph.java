/*
 * Created on Oct 18, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

@SuppressWarnings("serial")
public class OrderedSparseMultigraph<V,E> 
    extends AbstractSparseGraph<V,E>
    implements Graph<V,E>, Serializable {
	
	public static final <V,E> Factory<Graph<V,E>> getFactory() { 
		return new Factory<Graph<V,E>> () {
			public Graph<V,E> create() {
				return new OrderedSparseMultigraph<V,E>();
			}
		};
	}
    protected Map<V, Pair<Set<E>>> vertices; // Map of vertices to Pair of adjacency sets {incoming, outgoing}
    protected Map<E, Pair<V>> edges;            // Map of edges to incident vertex pairs
    protected Set<E> directedEdges;

    public OrderedSparseMultigraph()
    {
        vertices = new LinkedHashMap<V, Pair<Set<E>>>();
        edges = new LinkedHashMap<E, Pair<V>>();
        directedEdges = new LinkedHashSet<E>();
    }

    public boolean containsVertex(V vertex) {
    	return vertices.keySet().contains(vertex);
    }
    
    public boolean containsEdge(E edge) {
    	return edges.keySet().contains(edge);
    }

    public Collection<E> getEdges()
    {
        return Collections.unmodifiableCollection(edges.keySet());
    }

    public Collection<V> getVertices()
    {
        return Collections.unmodifiableCollection(vertices.keySet());
    }

    public boolean addVertex(V vertex) {
        if (!vertices.containsKey(vertex)) {
            vertices.put(vertex, new Pair<Set<E>>(new LinkedHashSet<E>(), new LinkedHashSet<E>()));
            return true;
        } else {
        	return false;
        }
    }

    public boolean removeVertex(V vertex) {
        // copy to avoid concurrent modification in removeEdge
        Pair<Set<E>> i_adj_set = vertices.get(vertex);
        Pair<Set<E>> adj_set = new Pair<Set<E>>(new LinkedHashSet<E>(i_adj_set.getFirst()), 
                new LinkedHashSet<E>(i_adj_set.getSecond()));
        

//        Pair<Set<E>> adj_set = vertices.get(vertex);
        if (adj_set == null)
            return false;
        
        for (E edge : adj_set.getFirst())
            removeEdge(edge);
        for (E edge : adj_set.getSecond())
            removeEdge(edge);
        
        vertices.remove(vertex);
        
        return true;
    }
    
    public boolean addEdge(E e, V v1, V v2) {
        return addEdge(e, v1, v2, EdgeType.UNDIRECTED);
    }
    
    public boolean addEdge(E edge, V v1, V v2, EdgeType edgeType) {
    	return addEdge(edge, new Pair<V>(v1, v2), edgeType);
    }

    public boolean addEdge(E edge, Pair<? extends V> endpoints) {
    	return addEdge(edge, endpoints, EdgeType.UNDIRECTED);
    }
    
    public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType) {
    	
        edges.put(edge, new Pair<V>(endpoints));
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        
        if (!vertices.containsKey(v1))
            this.addVertex(v1);
        
        if (!vertices.containsKey(v2))
            this.addVertex(v2);
        

        vertices.get(v1).getSecond().add(edge);        
        vertices.get(v2).getFirst().add(edge);        
        if(edgeType == EdgeType.DIRECTED) {
        	directedEdges.add(edge);
        } else {
          vertices.get(v1).getFirst().add(edge);        
          vertices.get(v2).getSecond().add(edge);        
        }

        if (edges.containsKey(edge)) {
            Pair<V> existingEndpoints = edges.get(edge);
            Pair<V> new_endpoints = new Pair<V>(v1, v2);
            if (!existingEndpoints.equals(new_endpoints)) {
                throw new IllegalArgumentException("EdgeType " + edge + 
                        " exists in this graph with endpoints " + v1 + ", " + v2);
            } else {
                return false;
            }
        }
        
        
        return true;

    }
    
    public boolean removeEdge(E edge)
    {
        if (!edges.containsKey(edge)) {
            return false;
        }
        
        Pair<V> endpoints = getEndpoints(edge);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        
        // remove edge from incident vertices' adjacency sets
        vertices.get(v1).getSecond().remove(edge);
        vertices.get(v2).getFirst().remove(edge);

        if(directedEdges.remove(edge) == false) {
        	
        	// its an undirected edge, remove the other ends
            vertices.get(v2).getSecond().remove(edge);
            vertices.get(v1).getFirst().remove(edge);
        }
        edges.remove(edge);
        return true;
    }
    
    public Collection<E> getInEdges(V vertex)
    {
        return Collections.unmodifiableCollection(vertices.get(vertex).getFirst());
    }

    public Collection<E> getOutEdges(V vertex)
    {
        return Collections.unmodifiableCollection(vertices.get(vertex).getSecond());
    }

    public Collection<V> getPredecessors(V vertex)
    {
        Set<E> incoming = vertices.get(vertex).getFirst();        
        Set<V> preds = new LinkedHashSet<V>();
        for (E edge : incoming) {
        	if(getEdgeType(edge) == EdgeType.DIRECTED) {
        		preds.add(this.getSource(edge));
        	} else {
        		preds.add(getOpposite(vertex, edge));
        	}
        }
        return Collections.unmodifiableCollection(preds);
    }

    public Collection<V> getSuccessors(V vertex)
    {
        Set<E> outgoing = vertices.get(vertex).getSecond();        
        Set<V> succs = new LinkedHashSet<V>();
        for (E edge : outgoing) {
        	if(getEdgeType(edge) == EdgeType.DIRECTED) {
        		succs.add(this.getDest(edge));
        	} else {
        		succs.add(getOpposite(vertex, edge));
        	}
        }
        return Collections.unmodifiableCollection(succs);
    }

    public Collection<V> getNeighbors(V vertex)
    {
        Collection<V> out = new LinkedHashSet<V>();
        out.addAll(this.getPredecessors(vertex));
        out.addAll(this.getSuccessors(vertex));
        return out;
    }

    public Collection<E> getIncidentEdges(V vertex)
    {
        Collection<E> out = new LinkedHashSet<E>();
        out.addAll(this.getInEdges(vertex));
        out.addAll(this.getOutEdges(vertex));
        return out;
    }

    public E findEdge(V v1, V v2)
    {
        Set<E> outgoing = vertices.get(v1).getSecond();
        for (E edge : outgoing)
            if (this.getOpposite(v1, edge).equals(v2))
                return edge;
        
        return null;
    }

    public Pair<V> getEndpoints(E edge)
    {
        return edges.get(edge);
    }

    public V getSource(E edge) {
    	if(directedEdges.contains(edge)) {
    		return this.getEndpoints(edge).getFirst();
    	}
    	return null;
    }

    public V getDest(E edge) {
    	if(directedEdges.contains(edge)) {
    		return this.getEndpoints(edge).getSecond();
    	}
    	return null;
    }

    public boolean isSource(V vertex, E edge) {
    	if(directedEdges.contains(edge)) {
    		return vertex.equals(this.getEndpoints(edge).getFirst());
    	}
    	return false;
    }

    public boolean isDest(V vertex, E edge) {
    	if(directedEdges.contains(edge)) {
    		return vertex.equals(this.getEndpoints(edge).getSecond());
    	}
    	return false;
    }

    public EdgeType getEdgeType(E edge) {
    	return directedEdges.contains(edge) ?
    		EdgeType.DIRECTED :
    			EdgeType.UNDIRECTED;
    }

	public Collection<E> getEdges(EdgeType edgeType) {
		if(edgeType == EdgeType.DIRECTED) {
			return Collections.unmodifiableSet(this.directedEdges);
		} else if(edgeType == EdgeType.UNDIRECTED) {
			Collection<E> edges = new LinkedHashSet<E>(getEdges());
			edges.removeAll(directedEdges);
			return edges;
		} else {
			return Collections.EMPTY_SET;
		}
		
	}

	public int getEdgeCount() {
		return edges.keySet().size();
	}

	public int getVertexCount() {
		return vertices.keySet().size();
	}

}
