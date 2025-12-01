import { create } from 'zustand';
import type { Cart, CartItem, AddOn } from '@/types';

interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  setCart: (cart: Cart) => void;
  addItem: (item: CartItem) => void;
  removeItem: (itemId: string) => void;
  updateItem: (itemId: string, updates: Partial<CartItem>) => void;
  addAddOn: (itemId: string, addOn: AddOn) => void;
  removeAddOn: (itemId: string, addOnId: string) => void;
  clearCart: () => void;
  setLoading: (loading: boolean) => void;
  getItemCount: () => number;
  getTotalAmount: () => number;
}

export const useCartStore = create<CartState>()((set, get) => ({
  cart: null,
  isLoading: false,

  setCart: (cart) => set({ cart }),

  addItem: (item) =>
    set((state) => {
      if (!state.cart) return state;
      return {
        cart: {
          ...state.cart,
          items: [...state.cart.items, item],
          totalAmount: state.cart.totalAmount + item.price,
        },
      };
    }),

  removeItem: (itemId) =>
    set((state) => {
      if (!state.cart) return state;
      const item = state.cart.items.find((i) => i.id === itemId);
      if (!item) return state;
      return {
        cart: {
          ...state.cart,
          items: state.cart.items.filter((i) => i.id !== itemId),
          totalAmount: state.cart.totalAmount - item.price,
        },
      };
    }),

  updateItem: (itemId, updates) =>
    set((state) => {
      if (!state.cart) return state;
      return {
        cart: {
          ...state.cart,
          items: state.cart.items.map((item) =>
            item.id === itemId ? { ...item, ...updates } : item
          ),
        },
      };
    }),

  addAddOn: (itemId, addOn) =>
    set((state) => {
      if (!state.cart) return state;
      return {
        cart: {
          ...state.cart,
          items: state.cart.items.map((item) =>
            item.id === itemId
              ? { ...item, addOns: [...item.addOns, addOn] }
              : item
          ),
          totalAmount: state.cart.totalAmount + addOn.price,
        },
      };
    }),

  removeAddOn: (itemId, addOnId) =>
    set((state) => {
      if (!state.cart) return state;
      const item = state.cart.items.find((i) => i.id === itemId);
      const addOn = item?.addOns.find((a) => a.id === addOnId);
      if (!addOn) return state;
      return {
        cart: {
          ...state.cart,
          items: state.cart.items.map((item) =>
            item.id === itemId
              ? { ...item, addOns: item.addOns.filter((a) => a.id !== addOnId) }
              : item
          ),
          totalAmount: state.cart.totalAmount - addOn.price,
        },
      };
    }),

  clearCart: () =>
    set((state) => {
      if (!state.cart) return state;
      return {
        cart: {
          ...state.cart,
          items: [],
          totalAmount: 0,
        },
      };
    }),

  setLoading: (isLoading) => set({ isLoading }),

  getItemCount: () => {
    const cart = get().cart;
    return cart?.items.length ?? 0;
  },

  getTotalAmount: () => {
    const cart = get().cart;
    return cart?.totalAmount ?? 0;
  },
}));
