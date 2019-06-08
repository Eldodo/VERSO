package oclruler.rule.struct;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import oclruler.metamodel.Concept;
import oclruler.metamodel.Metamodel;
import oclruler.rule.struct.Node.Type;
import oclruler.utils.Config;
import oclruler.utils.ToolBox;

/**
 * 
 * @author Edouard Batot 2016 - batotedo@iro.umontreal.ca
 *
 */
public class NodeFactory {
	private static Logger LOGGER = Logger.getLogger(NodeFactory.class.getName());

	public static boolean FOs_NESTING = true;

	/**
	 * Maximum depth of nodes at random creation.
	 */
	public static int CREATION_DEPTH = 5;
	/**
	 * if <code>true</code> random creation will force the depth to {@link NodeFactory.CREATION_DEPTH CREATION_Depth}
	 */
	public static boolean FORCE_CREATION_DEPTH = false;

	/**
	 * Load <code>CREATION_DEPTH</code> ans <code>FORCE_CREATION_DEPTH</code> from config file.
	 */
	public static void loadConfig() {
		try {
			CREATION_DEPTH = Config.getIntParam("CREATION_DEPTH");
		} catch (Exception e) {
			String s = Config.getStringParam("CREATION_DEPTH");
			try {
				if (s.startsWith("f"))
					s = s.substring(1);
				CREATION_DEPTH = Integer.parseInt(s);
				FORCE_CREATION_DEPTH = true;
			} catch (Exception e1) {
				LOGGER.warning("" + e1.getMessage());
			}
		}
		if (CREATION_DEPTH < 2) {
			LOGGER.warning("RandomNode creation will only produce DEFAULT node : CREATION_DEPTH < 2");
		}
	}

	/**
	 * Deprecated, please favor {@link Constraint#prune()}.
	 * 
	 * @param n
	 * @return
	 */
	static int loop = 1;

	@Deprecated
	public static Node pruneNode(Node n) throws Exception {
		n.prune();
		if (n.toBePruned()) {
			Node mid = (n.type == Type.NOT) ? Node.collapseChildren(n) : n;
			n = Node.collapseChildren(mid);
		}
		return n;
	}

	/**
	 * Create an empty node with a parent, a context and a type (null if root)
	 * 
	 * @param parent
	 * @param context
	 * @param type
	 * @return
	 */
	public static Node createEmptyNode(Node parent, Concept context, Node.Type type) {
		Node n = null;
		try {
			if (type.isOperator()) {
				n = new Node(parent, context, type);

			} else
				// DEFAULT and FO and TRUE
				n = type.getNodeClass().getDeclaredConstructor(Node.class, Concept.class).newInstance(parent, context);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
		return n;
	}

	/**
	 * Creation a Node with a parent, a context, a defined type and depth and declare if its embedded in a {@link Node_FO}.<br/>
	 * <ul>
	 * 	<li><code>null</code> parameters will be set randomly.</li>
	 * 	<li>If the newly created node is a FO, a random {@link Concept#getSimplePaths() simple path} will be used.</li>
	 * <ul>
	 *
	 * @param parent Node parent in the tree (null if this is a root). 
	 * @param context Concept.
	 * @param type See {@link Node.Type}
	 * @param depth Maximum depth of the node (if -1, random integer picked in [1..{@link NodeFactory#CREATION_DEPTH}])
	 * @param inFO Node_FO in which the node is imbricated (if not null its {@link Node_FO#subname subName} will be used as context in its {@link Node#children sub nodes}.). 
	 * 
	 * @return
	 */
	public static Node createNode(Node parent, Concept context, Node.Type type, int depth, Node_FO inFO) {
		if (context == null)
			throw new IllegalArgumentException("Context must be instantiated.");

		if (depth < 0) {// Random depth
			if (CREATION_DEPTH < 2)
				type = Type.DEFAULT;
			else
				depth = FORCE_CREATION_DEPTH ? CREATION_DEPTH : ToolBox.getRandomInt(1, CREATION_DEPTH);
		}

		if (depth == 1)
			type = Type.DEFAULT;// to avoid inconsistency between depth and type

		if (type == null) {// Random type
			type = Type.DEFAULT;
			if (depth > 1) {
				ArrayList<Type> exclusion = new ArrayList<>(2);
				exclusion.add(Type.TRUE);
				if (!FOs_NESTING && inFO != null)// Avoid FOs nesting.
					exclusion.add(Type.FO);
				if (FORCE_CREATION_DEPTH)
					exclusion.add(Type.DEFAULT);// Type default will not allow more descendance (no children). If
												// FORCE_CREATION_DEPTH, avoid this type.
				type = ToolBox.getRandom(Type.valuesNot(exclusion));
			}
		}

		Node n = createEmptyNode(parent, context, type);

		if (n == null)
			throw new IllegalArgumentException("Node couldn't be created with : parent=" + parent + ", context=" + context + ", type=" + type);

		int minNextDepth = 0;
		switch (type) {
		case AND:
		case OR:
		case NOT:
		case IMPLIES:
			minNextDepth = 1;
			break;
		case FO:
			boolean success = ((Node_FO) n).randomCompletion();

			if (success) {
				context = ((Node_FO) n).getPath().getEndType();// To be transmit to sub Nodes

				minNextDepth = 1;
				if (inFO != null)
					n.setSelfOrSubName(inFO.subname);
				inFO = (Node_FO) n;

			} else {
				type = Type.NOT;
				int idx = n.getChildIndex();
				n = createEmptyNode(null, context, type);// Node_FO cannot find a path (Enum case) we make it a Node_NOT
				if (parent != null) {
					parent.setChild(idx, n);
					n.setParent(parent);
				}
			}
			break;
		case DEFAULT:
			boolean success2 = ((Node_DEFAULT) n).randomCompletion();
			if (success2) {
				if (inFO != null)
					((Node_DEFAULT) n).getPattern().setSelfOrSubname(inFO.getSubName());
			} else {
				type = Type.TRUE;
				int idx = n.getChildIndex();
				n = null;
				n = createEmptyNode(null, context, type);
				if (parent != null) {
					parent.setChild(idx, n);
					n.setParent(parent);
				}
			}
			break;
		default:
			throw new IllegalArgumentException("Illegal type = " + type + ". Allowed are : " + Arrays.deepToString(Type.values()));
		}

		minNextDepth = Math.max(minNextDepth, (int) Math.ceil((depth - 1) * (double) 4 / 5));

		if (depth > 1) {// Double check as (depth > 1) shouldn't be if (|children| == 0), should it be ?
			for (int i = 0; i < type.getNumberOfChildren(); i++) {
				// n.addChild(
				NodeFactory.createRandomNode(n, context, FORCE_CREATION_DEPTH ? (depth - 1) : ToolBox.getRandomInt(minNextDepth, depth - 1), inFO);
			}
		}
		try {
			n = NodeFactory.pruneNode(n);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return n;
	}
	

	/**
	 * @param n
	 */
	public static String checkChildOfParent(Node n) {
		String res = "";
		if (n.parent != null) {
			if (n.parent.children.indexOf(n) < 0) {
				res = "\nNodeFactory.checkLineage(" + n.getId() + ") ";
				res += "\nthis:   " + n.getId() + " (p: " + n.parent.getId() + ")\n";
				String s = "parent.children : {";
				for (Node node : n.parent.children) {
					s += node.getId() + " ";
				}
				s = s.trim() + "}";
				res += s;
			}
		}
		return res;
	}

	/**
	 * Creates a node which depth is comprised in [1..{@link NodeFactory#CREATION_DEPTH}]
	 * 
	 * @param parent
	 *            Node parent in the tree.
	 * @param context
	 *            Concept.
	 * @param type
	 *            See {@link Node.Type}
	 * @param inFO
	 *            Node_FO in which the node is imbricated (if not null its {@link Node_FO#subname subName} will be used
	 *            as context in its {@link Node#children sub nodes}.).
	 * 
	 * @return
	 */
	public static Node createRandomNode(Node parent, Concept context, Node.Type type, Node_FO inFO) {
		return createNode(parent, context, type, -1, inFO);
	}

	/**
	 * Creates randomly a node of depth <code>depth</code>.<br/>
	 * Avoids FOs' imbrication. (Namespace limitation)
	 * 
	 * @param depth
	 *            if equals 1, Type will be DEFAULT.
	 * @return
	 */
	public static Node createRandomNode(Node parent, Concept context, int depth, Node_FO inFO) {
		return createNode(parent, context, null, depth, inFO);
	}

	/**
	 * Creates randomly a node of depth between 1 and <code>CREATION_DEPTH</code>.
	 * 
	 * @return
	 */
	public static Node createRandomNode(Node parent, Concept context, Node_FO inFO) {
		return createNode(parent, context, null, -1, inFO);
	}

	public static Node createRandomNode(Concept context) {
		return createNode(null, context, null, -1, null);
	}

	public static Node createRandomNode() {
		return createNode(null, ToolBox.getRandom(Metamodel.getAuthorizedConcepts()), null, -1, null);
	}

	public static Node createRandomNode(Type type) {
		return createNode(null, ToolBox.getRandom(Metamodel.getAuthorizedConcepts()), type, -1, null);
	}

	public static Node createRandomNode(Type type, int depth) {
		return createNode(null, ToolBox.getRandom(Metamodel.getAuthorizedConcepts()), type, depth, null);
	}

}
