package concurrentcachesystem;

class Node<K>{
    Node next;
    Node prev;
    K key;

    public Node(K key){
        this.key = key;
    }
}