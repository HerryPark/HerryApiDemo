package com.herry.libs.nodeview.model


@Suppress("MemberVisibilityCanBePrivate", "unused")
object NodeHelper {

    fun <M : Any> createNode(model: M): Node<M> {
        return Node(model)
    }

    fun createNodeGroup(): Node<NodeModelGroup> {
        return Node(NodeModelGroup())
    }

    fun <T : INodeModel> getNode(model: T): Node<T>? {
        @Suppress("UNCHECKED_CAST")
        return model.getNode() as? Node<T>
    }

    inline fun <reified T : Any> getChildNode(model: INodeModel): Node<T>? {
        model.getNode()?.let {
            return getChildNode(it)
        }
        return null
    }

    inline fun <reified T : Any> getChildNode(node: Node<*>): Node<T>? {
        @Suppress("UNCHECKED_CAST")
        return (0 until node.getChildCount())
            .map { i -> node.getChildNode(i) }
            .firstOrNull { it?.model is T } as? Node<T>
    }

    inline fun <reified T : INodeModelGroup> findChildNodeFromId(node: Node<*>, id: Any): Node<T>? {
        @Suppress("UNCHECKED_CAST")
        return (0 until node.getChildCount())
            .map { i -> node.getChildNode(i) }
            .filter {
                it?.model is T && (it.model as T).id == id
            }
            .firstOrNull { it?.model is T } as? Node<T>
    }

    inline fun <reified T : Any> getChildrenNode(model: INodeModel): MutableList<Node<T>> {
        model.getNode()?.let {
            return getChildrenNode(it)
        }
        return mutableListOf()
    }

    inline fun <reified T : Any> getChildrenNode(node: Node<*>): MutableList<Node<T>> {
        val list = mutableListOf<Node<T>>()

        (0 until node.getChildCount())
            .map { i -> node.getChildNode(i) }
            .filter { it?.model is T }
            .forEach {
                it?.run {
                    @Suppress("UNCHECKED_CAST")
                    list.add(it as Node<T>)
                }
            }
        return list
    }

    inline fun <reified T : Any> findChildrenNode(node: Node<*>, model: T): Node<T>? {
        return getChildrenNode<T>(node).firstOrNull { it.model == model }
    }

    inline fun <reified T : Any> getChildModel(model: INodeModel): T? {
        model.getNode()?.let {
            return getChildModel(it)
        }
        return null
    }

    inline fun <reified T : Any> getChildModel(node: Node<*>): T? {
        return (0 until node.getChildCount())
            .map { i -> node.getChildNode(i) }
            .firstOrNull { it?.model is T }?.model as? T
    }

    inline fun <reified T : Any> getChildrenModels(model: INodeModel): MutableList<T> {
        model.getNode()?.let {
            return getChildrenModels(it)
        }
        return mutableListOf()
    }

    inline fun <reified T : Any> getChildrenModels(node: Node<*>): MutableList<T> {
        val list = mutableListOf<T>()

        (0 until node.getChildCount())
            .map { i -> node.getChildNode(i) }
            .filter { it?.model is T }
            .forEach {
                it?.run {
                    @Suppress("UNCHECKED_CAST")
                    list.add(it.model as T)
                }
            }
        return list
    }


    fun <T : Any> addModel(parent: Node<*>, model: T): Node<T> {
        val node = createNode(model)
        addNode(parent, node)
        return node
    }

    fun <T : Any> addModel(parent: Node<*>, position: Int, model: T): Node<T> {
        val node = createNode(model)
        addNode(parent, position, node)
        return node
    }

    fun <T : Any> addModels(parent: Node<*>, vararg models: T) {
        val nodes = Array<Node<*>>(models.size) { createNode(models[it]) }
        addNodes(parent, nodes = nodes)
    }

    fun <T : Any> addModels(parent: Node<*>, position: Int, vararg models: T) {
        val nodes = Array<Node<*>>(models.size) { createNode(models[it]) }
        addNodes(parent, position, nodes = nodes)
    }

    fun addNode(parent: Node<*>, node: Node<*>) {
        parent.addChild(node)
    }

    fun addNode(parent: Node<*>, position: Int, node: Node<*>) {
        parent.addChild(node, position = position)
    }

    fun addNodes(parent: Node<*>, vararg nodes: Node<*>) {
        parent.addChild(*nodes)
    }

    fun addNodes(parent: Node<*>, position: Int, vararg nodes: Node<*>) {
        parent.addChild(*nodes, position = position)
    }

    fun addNodeGroup(parent: Node<*>): Node<NodeModelGroup> {
        val node = createNodeGroup()
        parent.addChild(node)
        return node
    }

    fun removeNode(node: Node<*>, notify: Boolean = false) {
        node.parent?.let {
            if (notify) {
                it.beginTransition()
                it.removeChild(node)
                it.endTransition()
            } else {
                it.removeChild(node)
            }
        }
    }

    fun removeNode(model: INodeModel) {
        model.getNode()?.let {
            it.parent?.removeChild(it)
        }
    }

    fun changedNode(model: INodeModel) {
        model.getNode()?.changedNode()
    }

    fun changedNode(node: Node<*>, notify: Boolean = false) {
        if (notify)
            node.beginTransition()
        node.changedNode()
        if (notify)
            node.endTransition()
    }

    inline fun <reified T : INodeModel> upSert(parent: Node<out INodeModel>, from: Node<T>?, to: Node<T>): Node<T> {
        return if (from != null) {
            from.replace(to)
            from
        } else {
            addNode(parent, to)
            to
        }
    }

    /**
     * Changes node (if 'from' is A and 'to' is B, Changes A to B).
     */
    inline fun <reified T : INodeModel> upSert(from: Node<T>, to: Node<T>) {
        from.replace(to)
    }

    inline fun <reified T : INodeModel> transition(node: Node<T>, block: (transitionNode: Node<T>) -> Unit) {
        node.beginTransition()
        block(node)
        node.endTransition()
    }
}