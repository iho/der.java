package com.iho.asn1;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ASN1NodeCollection implements Iterable<ASN1Node> {
    private final List<DERParser.ParserNode> allNodes;
    private final int start;
    private final int end;
    private final int depth;

    public ASN1NodeCollection(List<DERParser.ParserNode> allNodes, int start, int end, int depth) {
        this.allNodes = allNodes;
        this.start = start;
        this.end = end;
        this.depth = depth;
    }

    @Override
    public Iterator<ASN1Node> iterator() {
        return new ASN1NodeCollectionIterator(allNodes, start, end, depth);
    }

    private static class ASN1NodeCollectionIterator implements Iterator<ASN1Node> {
        private final List<DERParser.ParserNode> allNodes;
        private final int end;
        private final int depth;
        private int current;

        public ASN1NodeCollectionIterator(List<DERParser.ParserNode> allNodes, int start, int end, int depth) {
            this.allNodes = allNodes;
            this.current = start;
            this.end = end;
            this.depth = depth;
        }

        @Override
        public boolean hasNext() {
            return current < end;
        }

        @Override
        public ASN1Node next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int index = current;
            int subtreeEnd = findSubtreeEnd(index);
            current = subtreeEnd;
            return createNode(index, subtreeEnd);
        }

        public ASN1Node peek() {
            if (!hasNext()) {
                return null;
            }
            int index = current;
            int subtreeEnd = findSubtreeEnd(index);
            return createNode(index, subtreeEnd);
        }

        private int findSubtreeEnd(int index) {
            int nodeDepth = allNodes.get(index).depth;
            for (int i = index + 1; i < end; i++) {
                if (allNodes.get(i).depth <= nodeDepth) {
                    return i;
                }
            }
            return end;
        }

        private ASN1Node createNode(int index, int subtreeEnd) {
            DERParser.ParserNode pnode = allNodes.get(index);
            ASN1Node.Content content;
            if (pnode.isConstructed) {
                content = new ASN1Node.Constructed(new ASN1NodeCollection(allNodes, index + 1, subtreeEnd, pnode.depth));
            } else {
                content = new ASN1Node.Primitive(pnode.dataBytes);
            }
            return new ASN1Node(pnode.identifier, content, pnode.encodedBytes);
        }
    }
}
