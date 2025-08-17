-- Insert a restaurant with two products
-- Use plain SQL without PL/pgSQL blocks for better compatibility with Spring Boot

-- First, create the restaurant if it doesn't exist
INSERT INTO restaurant_schema.restaurants (restaurant_id, name, available, products, version)
SELECT
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'Kopytka Paradise',
    true,
    '[
      {
        "id": {
          "productId": "660e8400-e29b-41d4-a716-446655440001"
        },
        "name": "Tradycyjne Kopytka",
        "price": {
          "amount": 15.90
        },
        "available": true
      },
      {
        "id": {
          "productId": "770e8400-e29b-41d4-a716-446655440002"
        },
        "name": "Polskie Kopytka",
        "price": {
          "amount": 18.90
        },
        "available": true
      }
    ]'::jsonb,
    0
WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_schema.restaurants
    WHERE restaurant_id = '550e8400-e29b-41d4-a716-446655440000'::uuid
);