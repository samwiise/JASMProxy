/**
 * 
 */
package org.asmmr.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author asim.ali
 *
 */
public class ASMTreeNode 
{
	
	protected String name=null;
	protected Map< String, String> attributes = new Hashtable<String, String>();
	protected ASMTreeNode parent=null;
	protected List<ASMTreeNode> children=new ArrayList<ASMTreeNode>();
	
	protected String value="";
	
	public ASMTreeNode(String name){
		this.name =name;
	}
	
	public ASMTreeNode(String name,ASMTreeNode parent){
		this.name=name;
		this.parent =parent;
	}
	
	public void add(ASMTreeNode node) 
	{
		if(children.indexOf(node)<0) {
			node.setParent(this);
			children.add(node);
		}
	}
	
	public ASMTreeNode remove(ASMTreeNode node){
		ASMTreeNode ret=null;
		int index=-1;
		while((index=children.indexOf(node))>-1) 
		{
			ret = children.remove(index);
		}
		return ret;
	}  
	
	public void addAttribute(String name,String value) {
		attributes.put(name, value);
	}
	public String removeAttribute(String name) {
		return attributes.remove(name);
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	public List<ASMTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<ASMTreeNode> children) {
		this.children = children;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ASMTreeNode getParent() {
		return parent;
	}
	public void setParent(ASMTreeNode parent) {
		this.parent = parent;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void appendValue(String value) {
		this.value += value;
	}

	public ASMTreeNode getSingleNode(String regex)
	{
		List<ASMTreeNode> list = getNodes(regex);
		if(list.size()>0)
			return list.get(0);
		return null;
	}
	
	public List<ASMTreeNode> getNodes(String regex)
	{
		List<ASMTreeNode> nodes = new ArrayList<ASMTreeNode>();
		String fullPath = getFullPath(); 		
		
		if(fullPath.matches(regex))
			nodes.add(this);
		
		Iterator<ASMTreeNode> iterator = children.iterator();
		
		while(iterator.hasNext()){
			ASMTreeNode node = iterator.next();
			nodes.addAll(node.getNodes(regex));
		}
		
		return nodes;
	}	
	
	public String getFullPath()
	{
		String pString = this.name;
		if(parent!=null)
			pString = parent.getFullPath() + "/" + pString;
		
		return pString;
	}
	public static ASMTreeNode MakeTree(ASMTreeNode tree,Node node) 
	{
		NodeList nodeList =	node.getChildNodes();
		
		if(tree==null)
			tree=MakeTreeNode(node);
		
		int index=0;
		while(index<nodeList.getLength()) 
		{
			Node cNode = nodeList.item(index);
			
			if(cNode.getNodeType() == Node.ELEMENT_NODE)
			{
				/*ASMTreeNode aChildNode = new ASMTreeNode(cNode.getNodeName());
				
				NamedNodeMap nodeMap = cNode.getAttributes();
				
				int j =0;
				while(j<nodeMap.getLength()) {
					Node nd =	nodeMap.item(j);
					
					aChildNode.addAttribute(nd.getNodeName(),nd.getNodeValue());
					
					j++;
				}*/
				
				ASMTreeNode aChildNode = MakeTreeNode(cNode);
								
				tree.add(aChildNode);
				MakeTree(aChildNode, cNode);
			}else if (cNode.getNodeType() == Node.TEXT_NODE) {
				tree.appendValue(cNode.getNodeValue());
			}
			index++;				
		}
		
		return tree;
	}
	
	public static ASMTreeNode MakeTreeNode(Node node) 
	{
		ASMTreeNode tree = new ASMTreeNode(node.getNodeName());
		
		NamedNodeMap nodeMap = node.getAttributes();
		
		int j =0;
		while(j<nodeMap.getLength()) {
			Node nd =	nodeMap.item(j);
			tree.addAttribute(nd.getNodeName(),nd.getNodeValue());
			j++;
		}
		
		return tree;
	}

	
}
