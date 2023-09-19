package com.herry.libs.nodeview.model

@Suppress("MemberVisibilityCanBePrivate")
open class Node<M : Any> {

    private val isVisible: Boolean

    var isExpansion: Boolean
        get() = nodeModelGroup?.isExpansion ?: false
        set(value) {
            nodeModelGroup?.isExpansion = value
        }

    var model: M
        private set

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(model: M) {
        this.isVisible = model::class != NodeModelGroup::class
        setModel(value = model)
        this.model = model
    }

    private fun setModel(value: M, onCompareForChange: ((src: Any, dest: Any) -> Boolean)? = null) {
        if (value is INodeModelGroup) {
            this@Node.nodeModelGroup?.setOnGetNode(null)
            this@Node.nodeModelGroup?.setOnChangedExpansion(null)
            this@Node.nodeModelGroup = value
            this@Node.nodeModelGroup?.setOnGetNode {
                this@Node
            }
            this@Node.nodeModelGroup?.setOnChangedExpansion {
                changedNode()
                if (this@Node.nodeChildren.viewCount > 0) {
                    this@Node.parent?.run {
                        val param = NodeNotifyParam(
                            if (it.isExpansion) NodeNotifyParam.STATE.INSERT else NodeNotifyParam.STATE.REMOVE,
                            this@Node.viewPosition + if (this@Node.isVisible) 1 else 0,
                            this@Node.nodeChildren.viewCount
                        )

                        val success = this@Node.parent?.notify(param) {

                        } ?: true

                        if (!success) {
                            param.position = this@Node.viewPosition + if (this@Node.isVisible) 1 else 0
                            this@Node.parent?.notify(param, null)
                        }
                    }
                }
            }
        } else if (value is INodeModel) {
            this@Node.nodeModel?.setOnGetNode(null)
            this@Node.nodeModel = value
            this@Node.nodeModel?.setOnGetNode {
                this@Node
            }
        }

        val current = this.model
        this.model = value
        if (onCompareForChange?.invoke(current, value) ?: (current != value)) {
            changedNode()
        }
    }

    private var nodeModel: INodeModel? = null
    private var nodeModelGroup: INodeModelGroup? = null

    open var parent: Node<*>? = null
        internal set

    internal var viewPosition: Int = 0

    private var nodeId: Long = 0

    internal val nodeChildren: NodeChildren = NodeChildren(notify = fun(param: NodeNotifyParam, then: () -> Unit) {
        notifyFromChild(param, then)
    })

    private var updatedTraversal = false

    open fun beginTransition() {
        getRoot()?.beginTransition()
    }

    open fun endTransition() {
        getRoot()?.endTransition()
    }

    fun getViewPosition(): Int = viewPosition

    fun getViewCount(): Int {
        var viewCount = if (isVisible) 1 else 0
        if (isExpansion) {
            viewCount += nodeChildren.viewCount
        }
        return viewCount
    }

    open fun getRoot(): NodeRoot? = parent?.getRoot()

    internal fun getNodePosition(): NodePosition {
        var nodePosition = NodePosition(intArrayOf(viewPosition))
        var parentNode = parent
        while (parentNode != this) {
            if (parentNode == null) {
                return nodePosition
            }

            nodePosition = NodePosition.compose(parentNode.viewPosition, nodePosition)
            parentNode = parentNode.parent
        }

        return nodePosition
    }

    fun getNodePosition(viewPosition: Int): NodePosition? {
        if (viewPosition in 0 until getViewCount()) {
            var index = viewPosition
            if (isVisible) {
                if (index == 0) {
                    return null
                }
                index--
            }
            return nodeChildren.getNodePosition(index)
        }
        return null
    }

    fun getNode(nodePosition: NodePosition): Node<*>? {
        var result: Node<*>? = this
        for (position in nodePosition.positions) {
            if (result == null) {
                return null
            }
            result = result.getChildNode(position)
        }

        return result
    }

    internal fun setNodeId(root: NodeRoot) {
        nodeId = root.nextNodeId()
        nodeChildren.setNodeId(root)
    }

    internal fun setNodeId(nodeId: Long) {
        this.nodeId = nodeId
    }

    fun getNodeId(): Long = nodeId

    fun getChildCount(): Int = nodeChildren.getCount()

    fun isEmpty(): Boolean = getChildCount() == 0

    fun isNotEmpty(): Boolean = !isEmpty()

    fun getChildNode(position: Int): Node<*>? = nodeChildren.get(position)

    fun changedNode() {
        if (isVisible) {
            val param = NodeNotifyParam(
                NodeNotifyParam.STATE.CHANGE,
                viewPosition,
                1
            )
            parent?.run {
                val success = this.notify(param) {

                }
                if (!success) {
                    param.position = this@Node.viewPosition
                    this.notify(param, null)
                }
            }
        }
    }

    fun clearChild() {
        nodeChildren.clear()
    }

    fun addChild(vararg nodes: Node<*>, position: Int = nodeChildren.getCount()) {
        addChild(nodes.toMutableList(), position)
    }

    fun addChild(nodes: MutableList<Node<*>>, position: Int = nodeChildren.getCount()) {
        if (nodeModelGroup == null) {
            return
        }
        nodeChildren.add(this@Node, nodes, position)
    }

    fun removeChild(node: Node<*>) {
        val index = nodeChildren.indexOf(node)
        if (index >= 0) {
            removeChild(index, 1)
        }
    }

    fun removeChild(position: Int, count: Int) {
        nodeChildren.remove(position, count)
    }

    fun replace(node: Node<*>, onCompareForChange: ((src: Any, dest: Any) -> Boolean)? = null) {
        if (node.model is INodeModelGroup) {
            val nodeModel = node.model as INodeModelGroup
            val childList = mutableListOf<Node<*>>()
            for (index in 0 until node.getChildCount()) {
                node.getChildNode(index)?.let {
                    childList.add(it)
                }
            }

            if (isExpansion != nodeModel.isExpansion) {
                // notify all data changed
                nodeChildren.clear()
                @Suppress("UNCHECKED_CAST")
                setModel(value = node.model as M, onCompareForChange = onCompareForChange)
                nodeChildren.add(this@Node, childList)
            } else {
                @Suppress("UNCHECKED_CAST")
                setModel(value = node.model as M, onCompareForChange = onCompareForChange)
                nodeChildren.replace(parent = this@Node, nodeChildren = node.nodeChildren, onCompareForChange = onCompareForChange)
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            setModel(value = node.model as M, onCompareForChange = onCompareForChange)
        }
    }

    internal open fun notifyFromChild(param: NodeNotifyParam, then: (() -> Unit)?) {
        val parent = this.parent
        if (isExpansion && parent != null) {
            val success = parent.notify(NodeNotifyParam(param.state, param.position + viewPosition + if (isVisible) 1 else 0, param.count), then)
            if (!success) {
                parent.notify(NodeNotifyParam(param.state, param.position + viewPosition + if (isVisible) 1 else 0, param.count), null)
            }
        } else {
            then?.let { it() }
        }
    }

    internal open fun notify(param: NodeNotifyParam, then: (() -> Unit)?): Boolean {
        if (isExpansion) {
            if (param.state == NodeNotifyParam.STATE.INSERT || param.state == NodeNotifyParam.STATE.REMOVE) {
                updatedTraversal = false
            }

            parent?.let {
                param.position += (viewPosition + if (isVisible) 1 else 0)
                return it.notify(param, then)
            } ?: then?.let { it() }
        } else {
            then?.let { it() }
        }
        return true
    }

    internal open fun traversals(): Int {
        if (!updatedTraversal) {
            updatedTraversal = true
            nodeChildren.traversals()
        }
        return if (isVisible) 1 else 0 + if (isExpansion) nodeChildren.viewCount else 0
    }
}